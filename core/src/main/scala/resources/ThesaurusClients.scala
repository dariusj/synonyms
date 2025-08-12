package synonyms.core.resources

import cats.effect.{Async, Resource}
import cats.syntax.parallel.given
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.http4s.client.Client
import org.typelevel.log4cats.Logger
import synonyms.core.clients.JsoupParsable.given
import synonyms.core.clients.{JsoupParsable, ThesaurusClient}
import synonyms.core.domain.*
import synonyms.core.domain.Thesaurus.*

trait ThesaurusClients[F[_]]:
  def clients: Map[Thesaurus, ThesaurusClient[F]]

object ThesaurusClients:
  def make[F[_]: Async: Logger](client: Client[F]): Resource[F, ThesaurusClients[F]] =
    (
      ThesaurusClient.makeJsoup(Cambridge, JsoupBrowser()),
      ThesaurusClient.makeJsoup(MerriamWebster, JsoupBrowser()),
      ThesaurusClient.makeJsoup(PowerThesaurus, JsoupBrowser()),
      ThesaurusClient.makeJsoup(WordHippo, JsoupBrowser()),
      ThesaurusClient.makeStreaming(Datamuse, client)
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
