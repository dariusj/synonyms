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
      ThesaurusClient.makeJsoup(Cambridge),
      ThesaurusClient.makeJsoup(MerriamWebster),
      ThesaurusClient.makeJsoup(PowerThesaurus),
      ThesaurusClient.makeJsoup(WordHippo),
      ThesaurusClient.makeJson(Datamuse, client)
    ).parMapN { case (cambridge, mw, powerThesaurus, wordhippo, datamuse) =>
      new ThesaurusClients[F]:
        val clients: Map[Thesaurus, ThesaurusClient[F]] =
          Map(
            Cambridge      -> cambridge,
            MerriamWebster -> mw,
            PowerThesaurus -> powerThesaurus,
            WordHippo      -> wordhippo,
            Datamuse       -> datamuse
          )
    }
