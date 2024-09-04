package synonyms.thesaurus

import cats.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.option.*
import cats.syntax.parallel.*
import cats.syntax.traverse.*
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.response.Result
import synonyms.thesaurus.response.Result.*

class Service[F[_]: Monad: Parallel]:

  def checkSynonyms2(
      first: Word,
      second: Word,
      clients: List[Client[F]]
  ): F[Result] =
    clients
      .parTraverse(c => checkSynonyms(first, second, c))
      .map(f => f.reduce(_ combine _))

  def getEntries2(
      word: Word,
      clients: List[Client[F]]
  ): F[List[Entry]] =
    clients.parFlatTraverse(client => getEntries(word, client))

  def getEntries(
      word: Word,
      client: Client[F]
  ): F[List[Entry]] = client
    .fetchDocument(word)
    .flatMap(_.traverse(doc => client.buildEntries(word, doc)).map(_.orEmpty))

  def checkSynonyms(first: Word, second: Word, client: Client[F]): F[Result] =
    def areSynonyms(
        word: Word,
        candidate: Word,
        entries: List[Entry]
    ): Result =
      entries.foldLeft[Result](NotSynonyms(word, candidate)) {
        case (_: NotSynonyms, entry) => entry.hasSynonym(candidate)
        case (found: AreSynonyms, _) => found
      }

    for
      firstEntries <- getEntries(first, client)
      firstResult = areSynonyms(first, second, firstEntries)
      secondEntries <- getEntries(second, client)
      secondResult = areSynonyms(second, first, secondEntries)
    yield firstResult.combine(secondResult)
