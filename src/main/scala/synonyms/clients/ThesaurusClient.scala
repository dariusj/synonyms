package synonyms.clients

import _root_.io.circe.*
import cats.effect.*
import cats.syntax.applicativeError.*
import cats.syntax.either.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.option.*
import fs2.*
import fs2.data.json.*
import fs2.data.json.circe.*
import fs2.data.json.codec.*
import fs2.io.net.Network
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.model.Document
import org.http4s.*
import org.http4s.client.Client
import org.jsoup.HttpStatusException
import org.typelevel.log4cats.Logger
import synonyms.domain.Thesaurus.Datamuse
import synonyms.domain.{Entry, Thesaurus, Word}

import scala.concurrent.duration.*

trait ThesaurusClient[F[_]]:
  type Doc
  def fetchDocument(word: Word): F[Option[Doc]]
  def parseDocument(word: Word, document: Doc): F[List[Entry]]

object ThesaurusClient:
  def makeJsoup[F[_]: Sync: Logger, T <: Thesaurus](thesaurus: T)(using
      jsoupParsable: JsoupParsable[F, T],
      clock: Clock[F]
  ): Resource[F, ThesaurusClient[F]] =
    Resource.pure(
      new ThesaurusClient[F]:
        val browser: Browser = JsoupBrowser()
        type Doc = Document

        override def fetchDocument(word: Word): F[Option[Doc]] =
          for
            uri <- thesaurus.uri(word).liftTo[F]
            fetch = Sync[F].delay(browser.get(uri.renderString)).map(_.some).recover {
              case e: HttpStatusException if e.getStatusCode == 404 => None
            }
            _                    <- Logger[F].debug(s"Fetching $uri")
            (duration, maybeDoc) <- clock.timed(fetch)
            _                    <- Logger[F].debug(s"Fetching $uri took ${duration.toMillis} ms")
          yield maybeDoc

        override def parseDocument(word: Word, document: Doc): F[List[Entry]] =
          jsoupParsable.parseDocument(word, document)
    )

  def makeJson[F[_]: Async: Network: Logger, T <: Thesaurus](thesaurus: T, client: Client[F])(using
      jsonParsable: JsonParsable[F, T],
      clock: Clock[F]
  ): Resource[F, ThesaurusClient[F]] =
    Resource.pure(
      new ThesaurusClient[F]:
        override type Doc = List[Datamuse.Word]

        override def fetchDocument(word: Word): F[Option[Doc]] =
          val words: Stream[F, Doc] = for
            uri     <- Stream.fromEither(thesaurus.uri(word))
            request <- Stream(Request[F](Method.GET, uri))
            _       <- Stream.eval(Logger[F].debug(s"Fetching ${request.uri.renderString}"))
            start   <- Stream.eval(Clock[F].monotonic)
            res     <- client.stream(request)
            body <- res.body.through(text.utf8.decode).through(tokens).through(deserialize[F, Doc])
            end  <- Stream.eval(Clock[F].monotonic)
            _ <- Stream.eval(
              Logger[F].debug(
                s"Fetching ${request.uri.renderString} took ${(end - start).toMillis} ms"
              )
            )
          yield body
          // TODO: Error handling
          words.compile.toList.map(l => Option(l.head))

        override def parseDocument(word: Word, document: Doc): F[List[Entry]] =
          jsonParsable.parseDocument(word, document)
    )
