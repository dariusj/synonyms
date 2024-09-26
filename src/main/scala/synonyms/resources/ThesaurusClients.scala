package synonyms.resources

import cats.effect.{Async, Concurrent, Resource}
import cats.syntax.parallel.given
import fs2.io.net.Network
import org.http4s.client.Client
import org.typelevel.log4cats.Logger
import synonyms.clients.JsoupParsable.given
import synonyms.clients.{JsoupParsable, ThesaurusClient}
import synonyms.domain.*
import synonyms.domain.Thesaurus.*

trait ThesaurusClients[F[_]]:
  def clients: Map[Thesaurus, ThesaurusClient[F]]

object ThesaurusClients:
  def make[F[_]: Async: Concurrent: Network: Logger](
      client: Client[F]
  ): Resource[F, ThesaurusClients[F]] =
    (
      ThesaurusClient.makeJsoup[F, Cambridge](Cambridge),
      ThesaurusClient.makeJsoup[F, MerriamWebster](MerriamWebster),
      ThesaurusClient.makeJson[F, Datamuse](Datamuse, client)
    ).parMapN { case (cambridge, mw, datamuse) =>
      new ThesaurusClients[F]:
        val clients: Map[Thesaurus, ThesaurusClient[F]] =
          Map(
            Cambridge      -> cambridge,
            MerriamWebster -> mw,
            Datamuse       -> datamuse
          )
    }
