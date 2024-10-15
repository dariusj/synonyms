package synonyms.core.clients

import _root_.io.github.iltotore.iron.*
import cats.data.OptionT
import cats.effect.IO
import cats.syntax.either.*
import fs2.*
import munit.CatsEffectSuite
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import org.http4s.client.Client
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.{HttpRoutes, Response, Status}
import org.jsoup.HttpStatusException
import org.jsoup.nodes.Document
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.noop.NoOpLogger
import synonyms.core.clients.HttpStatusException as LocalHttpStatusException
import synonyms.core.domain.Thesaurus.{Datamuse, MerriamWebster}
import synonyms.core.domain.Word

import java.io.{File, IOException, InputStream}

class ThesaurusClientSuite extends CatsEffectSuite:
  given SelfAwareStructuredLogger[IO] = NoOpLogger[IO]

  jsoupClientFixture(defaultDocument).test(
    "Jsoup ThesaurusClient.fetchDocument returns a document"
  ) { case (client, document) =>
    client.fetchDocument(Word("foo")).map { maybeDoc =>
      assertEquals[Any, Any](maybeDoc, Some(document))
    }
  }

  errorJsoupClientFixture(HttpStatusException("Not Found", 404, "")).test(
    "Jsoup ThesaurusClient.fetchDocument returns None when request returns a 404"
  ) { case (client, _) =>
    client.fetchDocument(Word("foo")).map { maybeDoc => assertEquals(maybeDoc, None) }
  }

  errorJsoupClientFixture(HttpStatusException("Internal Server Error", 500, "")).test(
    "Jsoup ThesaurusClient.fetchDocument rethrows when request responds with a non-404 error"
  ) { case (client, exception) =>
    interceptIO[HttpStatusException](client.fetchDocument(Word("foo")))
  }

  streamingClientFixture(Status.Ok("bar")).test(
    "Streaming ThesaurusClient.fetchDocument returns a document"
  ) { client =>
    client.fetchDocument(Word("foo")).flatMap {
      // TODO: Tidy up {is/as}InstanceOf
      case Some(doc) if doc.isInstanceOf[Stream[?, ?]] =>
        val stream = doc.asInstanceOf[Stream[IO, Byte]]
        val list   = stream.through(text.utf8.decode).fold("")(_ + _).compile.toList
        assertIO(list, List("bar"))
      case value => IO(fail(s"got $value"))
    }
  }

  streamingClientFixture(Status.NotFound("Not Found")).test(
    "Streaming ThesaurusClient.fetchDocument returns None when request returns a 404"
  ) { client =>
    client.fetchDocument(Word("foo")).flatMap {
      case Some(any) if any.isInstanceOf[Stream[?, ?]] =>
        val stream = any.asInstanceOf[Stream[IO, Byte]]
        val io     = stream.through(text.utf8.decode).fold("")(_ + _).compile.toList
        io.map(list => fail(s"got $list, expected None"))
      case Some(doc) => IO(fail(s"got $doc"))
      case None      => IO(true)
    }
  }

  streamingClientFixture(Status.InternalServerError("Internal Server Error")).test(
    "Streaming ThesaurusClient.fetchDocument rethrows when request responds with a non-404 error"
  ) { client =>
    interceptMessageIO[LocalHttpStatusException]("Got 500: Internal Server Error")(
      client.fetchDocument(Word("foo"))
    )
  }

  private def streamingClientFixture(response: IO[Response[IO]]) =
    val httpApp = HttpRoutes.liftF(OptionT.liftF(response)).orNotFound
    val client  = Client.fromHttpApp[IO](httpApp)
    ResourceFunFixture(ThesaurusClient.makeStreaming[IO, Datamuse](Datamuse, client))

  private val defaultDocument = JsoupDocument(Document(""))

  private def errorJsoupClientFixture(exception: IOException) =
    val browser = TestBrowser(Left(exception))
    ResourceFunFixture(
      ThesaurusClient.makeJsoup[IO, MerriamWebster](MerriamWebster, browser).map(_ -> exception)
    )

  private def jsoupClientFixture(document: JsoupDocument) =
    val browser = TestBrowser(Right(document))
    ResourceFunFixture(
      ThesaurusClient.makeJsoup[IO, MerriamWebster](MerriamWebster, browser).map(_ -> document)
    )

  private class TestBrowser(get: Either[IOException, JsoupDocument]) extends Browser:
    type DocumentType = JsoupDocument
    override def get(url: String): DocumentType                             = get.valueOr(throw _)
    override def userAgent: String                                          = ???
    override def post(url: String, form: Map[String, String]): DocumentType = ???
    override def parseFile(file: File, charset: String): DocumentType       = ???
    override def parseInputStream(inputStream: InputStream, charset: String): DocumentType = ???
    override def parseString(html: String): DocumentType                                   = ???
    override def cookies(url: String): Map[String, String]                                 = ???
    override def clearCookies(): Unit                                                      = ???
    override def withProxy(proxy: net.ruippeixotog.scalascraper.browser.Proxy): Browser    = ???
