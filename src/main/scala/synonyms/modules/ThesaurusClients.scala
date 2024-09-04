package synonyms.modules

import cats.effect.Async
import cats.effect.Concurrent
import cats.effect.Resource
import cats.syntax.parallel.given
import fs2.io.net.Network
import synonyms.clients.JsoupParsable
import synonyms.clients.JsoupParsable.given
import synonyms.clients.ThesaurusClient
import synonyms.domain.*
import synonyms.domain.Thesaurus.*

trait ThesaurusClients[F[_]]:
  def clients: Map[Thesaurus, ThesaurusClient[F]]

object ThesaurusClients:
  def make[F[_]: Async: Concurrent: Network]: Resource[F, ThesaurusClients[F]] =
    (
      ThesaurusClient.makeJsoup[F, Cambridge](Cambridge),
      ThesaurusClient.makeJsoup[F, MerriamWebster](MerriamWebster),
      ThesaurusClient.makeJson[F, Datamuse](Datamuse)
    ).parMapN { case (cambridge, mw, datamuse) =>
      new ThesaurusClients[F]:
        val clients: Map[Thesaurus, ThesaurusClient[F]] =
          Map(
            Cambridge      -> cambridge,
            MerriamWebster -> mw,
            Datamuse       -> datamuse
          )
    }
