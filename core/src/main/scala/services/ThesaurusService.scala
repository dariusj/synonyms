package synonyms.core.services

import cats.*
import cats.effect.MonadCancelThrow
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.option.*
import cats.syntax.traverse.*
import synonyms.core.domain.*
import synonyms.core.resources.ThesaurusClients

trait ThesaurusService[F[_]]:
  def getEntries(word: Word, thesaurus: Thesaurus): F[List[Entry]]

object ThesaurusService:
  def make[F[_]: MonadCancelThrow](clients: ThesaurusClients[F]): ThesaurusService[F] =
    new ThesaurusService[F]:
      def getEntries(word: Word, thesaurus: Thesaurus): F[List[Entry]] =
        val client = clients.clients(thesaurus)
        client
          .fetchDocument(word)
          .flatMap(
            _.traverse(doc => client.parseDocument(word, doc)).map(_.orEmpty)
          )
