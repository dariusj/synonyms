package synonyms.clients

import _root_.io.circe.*
import cats.effect.Async
import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.applicativeError.*
import cats.syntax.functor.*
import cats.syntax.option.*
import fs2.*
import fs2.data.json.*
import fs2.data.json.circe.*
import fs2.data.json.codec.*
import fs2.io.net.Network
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import org.http4s.*
import org.http4s.ember.client.EmberClientBuilder
import org.jsoup.HttpStatusException
import synonyms.domain.Entry
import synonyms.domain.Thesaurus
import synonyms.domain.Thesaurus.Datamuse
import synonyms.domain.Word

trait ThesaurusClient[F[_]]:
  type Doc
  def fetchDocument(word: Word): F[Option[Doc]]
  def parseDocument(word: Word, document: Doc): F[List[Entry]]

object ThesaurusClient:
  def makeJsoup[F[_]: Sync, T <: Thesaurus](thesaurus: T)(using
      jsoupParsable: JsoupParsable[F, T]
  ): Resource[F, ThesaurusClient[F]] =
    Resource.pure(
      new ThesaurusClient[F]:
        val browser: Browser = JsoupBrowser()
        type Doc = Document

        override def fetchDocument(word: Word): F[Option[Doc]] =
          Sync[F]
            .delay(browser.get(thesaurus.url(word)))
            .map(_.some)
            .recover {
              case e: HttpStatusException if e.getStatusCode == 404 => None
            }

        override def parseDocument(word: Word, document: Doc): F[List[Entry]] =
          jsoupParsable.parseDocument(word, document)
    )

  def makeJson[F[_]: Async: Network, T <: Thesaurus](thesaurus: T)(using
      jsonParsable: JsonParsable[F, T]
  ): Resource[F, ThesaurusClient[F]] =
    Resource.pure(
      new ThesaurusClient[F]:
        override type Doc = List[Datamuse.Word]

        def request(word: Word): Request[F] =
          Request[F](Method.GET, Uri.unsafeFromString(thesaurus.url(word)))

        override def fetchDocument(word: Word): F[Option[Doc]] =
          val words: Stream[F, Doc] = for
            client <- Stream.resource(EmberClientBuilder.default[F].build)
            res    <- client.stream(request(word))
            body <- res.body.through(text.utf8.decode).through(tokens).through(deserialize[F, Doc])
          yield body
          // TODO: Error handling
          words.compile.toList.map(l => Option(l.head))

        override def parseDocument(word: Word, document: Doc): F[List[Entry]] =
          jsonParsable.parseDocument(word, document)
    )
