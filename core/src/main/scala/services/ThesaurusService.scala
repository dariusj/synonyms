package synonyms.core.services

import cats.*
import cats.data.OptionT
import cats.effect.MonadCancelThrow
import cats.syntax.functor.*
import cats.syntax.option.*
import cats.syntax.traverse.*
import monocle.syntax.all.*
import synonyms.core.domain.*
import synonyms.core.resources.ThesaurusClients

trait ThesaurusService[F[_]]:
  def getEntries(
      word: Word,
      thesaurus: Thesaurus,
      maxLength: SynonymLength,
      characterSet: CharacterSet
  ): F[List[Entry]]

object ThesaurusService:
  def make[F[_]: MonadCancelThrow](clients: ThesaurusClients[F]): ThesaurusService[F] =
    new ThesaurusService[F]:
      def getEntries(
          word: Word,
          thesaurus: Thesaurus,
          maxLength: SynonymLength,
          characterSet: CharacterSet
      ): F[List[Entry]] =
        val client                                      = clients.clients(thesaurus)
        def modifySynonyms(entry: Entry): Option[Entry] =
          val updatedEntry = entry
            .focus(_.synonyms)
            .modify(_.filter(_.countChars(characterSet) <= maxLength))
          updatedEntry.synonyms.headOption.as(updatedEntry)
        val maybeEntries = for
          doc             <- OptionT(client.fetchDocument(word))
          entries         <- OptionT.liftF(client.parseDocument(word, doc))
          modifiedEntries <- OptionT.fromOption(entries.traverse(modifySynonyms))
        yield modifiedEntries
        maybeEntries.value.map(_.orEmpty)
