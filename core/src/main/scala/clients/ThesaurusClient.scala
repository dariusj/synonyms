package synonyms.core.clients

import cats.effect.*
import cats.syntax.applicativeError.*
import cats.syntax.either.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.option.*
import fs2.*
import fs2.io.net.Network
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.model.Document
import org.http4s.*
import org.http4s.client.Client
import org.jsoup.HttpStatusException
import org.typelevel.log4cats.Logger
import synonyms.core.clients.HttpStatusException as LocalHttpStatusException
import synonyms.core.domain.{Entry, Thesaurus, Word}

import scala.concurrent.duration.*

trait ThesaurusClient[F[_]]:
  type Doc
  def fetchDocument(word: Word): F[Option[Doc]]
  def parseDocument(word: Word, document: Doc): F[List[Entry]]

object ThesaurusClient:
  def makeJsoup[F[_]: Sync: Logger, T <: Thesaurus](thesaurus: T, browser: Browser)(using
      jsoupParsable: JsoupParsable[F, T],
      clock: Clock[F]
  ): Resource[F, ThesaurusClient[F]] =
    Resource.pure(
      new ThesaurusClient[F]:
        type Doc = Document

        override def fetchDocument(word: Word): F[Option[Doc]] =
          for
            uri <- thesaurus.uri(word).liftTo[F]
            fetch = Sync[F].delay(browser.get(uri.renderString)).map(_.some).recover {
              case e: HttpStatusException if e.getStatusCode == 404 => None
            }
            _                    <- Logger[F].debug(s"Fetching $uri")
            (duration, maybeDoc) <- clock.timed(fetch)
            _                    <- Logger[F].debug(s"$uri responded in ${duration.toMillis} ms")
          yield maybeDoc

        override def parseDocument(word: Word, document: Doc): F[List[Entry]] =
          jsoupParsable.parseDocument(word, document)
    )

  def makeStreaming[F[_]: Async: Network: Logger, T <: Thesaurus](thesaurus: T, client: Client[F])(
      using streamingParsable: StreamingParsable[F, T]
  ): Resource[F, ThesaurusClient[F]] =
    Resource.pure(
      new ThesaurusClient[F]:
        override type Doc = Stream[F, Byte]

        def timed(request: Request[F]): Stream[F, (FiniteDuration, Response[F])] =
          for
            start <- Stream.eval(Clock[F].monotonic)
            res   <- client.stream(request)
            end   <- Stream.eval(Clock[F].monotonic)
          yield (end - start) -> res

        override def fetchDocument(word: Word): F[Option[Doc]] =
          def byteStream: Stream[F, Byte] =
            for
              uri                  <- Stream.fromEither(thesaurus.uri(word))
              request              <- Stream(Request[F](Method.GET, uri))
              _                    <- Stream.eval(Logger[F].debug(s"Fetching $uri"))
              (duration, response) <- timed(request)
              _ <- Stream.eval(Logger[F].debug(s"$uri responded in ${duration.toMillis} ms"))
              body <- response.status match
                case status if status.isSuccess   => response.body
                case status if status.code == 404 => Stream.empty
                case status =>
                  val body = response.bodyText.fold("")(_ + _).compile.toList.map(_.mkString(" "))
                  val exception = body.map(b => LocalHttpStatusException(status.code, b))
                  Stream.eval(exception).flatMap(ex => Stream.raiseError[F](ex))
            yield body
          val maybeBytes = byteStream.pull.uncons1
            .flatMap {
              case Some((head, tail)) => Pull.pure(Some(tail.cons1(head)))
              case None               => Pull.pure(None)
            }
            .flatMap(Pull.output1)
          maybeBytes.stream.compile.lastOrError

        override def parseDocument(word: Word, document: Doc): F[List[Entry]] =
          streamingParsable.parseDocument(word, document).compile.toList
    )
