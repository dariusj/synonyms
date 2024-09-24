package synonyms.services

import cats.*
import cats.effect.MonadCancelThrow
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.option.*
import cats.syntax.parallel.*
import cats.syntax.traverse.*
import synonyms.domain.*
import synonyms.domain.Result.*
import synonyms.modules.ThesaurusClients

trait Synonyms[F[_]]:
  def getEntries2(word: Word, thesauruses: List[Thesaurus]): F[List[Entry]]
  def getEntries(word: Word, thesaurus: Thesaurus): F[List[Entry]]
  def checkSynonyms2(first: Word, second: Word, thesauruses: List[Thesaurus]): F[Result]
  def checkSynonyms(first: Word, second: Word, thesaurus: Thesaurus): F[Result]

object Synonyms:
  def make[F[_]: MonadCancelThrow: Parallel](clients: ThesaurusClients[F]): Synonyms[F] =
    new Synonyms[F]:
      def getEntries2(word: Word, thesauruses: List[Thesaurus]): F[List[Entry]] =
        thesauruses.parFlatTraverse(thesaurus => getEntries(word, thesaurus))

      def getEntries(word: Word, thesaurus: Thesaurus): F[List[Entry]] =
        val client = clients.clients(thesaurus)
        client
          .fetchDocument(word)
          .flatMap(
            _.traverse(doc => client.parseDocument(word, doc)).map(_.orEmpty)
          )

      def checkSynonyms2(first: Word, second: Word, thesauruses: List[Thesaurus]): F[Result] =
        thesauruses
          .parTraverse(t => checkSynonyms(first, second, t))
          .map(f => f.reduce(_ combine _))

      def checkSynonyms(first: Word, second: Word, thesaurus: Thesaurus): F[Result] =
        def areSynonyms(word: Word, candidate: Word, entries: List[Entry]): Result =
          entries.foldLeft[Result](NotSynonyms(word, candidate)) {
            case (_: NotSynonyms, entry) => entry.hasSynonym(candidate)
            case (found: AreSynonyms, _) => found
          }

        for
          firstEntries <- getEntries(first, thesaurus)
          firstResult = areSynonyms(first, second, firstEntries)
          secondEntries <- getEntries(second, thesaurus)
          secondResult = areSynonyms(second, first, secondEntries)
        yield firstResult.combine(secondResult)
