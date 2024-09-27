package synonyms.http.routes

import cats.effect.*
import cats.syntax.show.*
import io.circe.*
import io.circe.syntax.*
import io.github.iltotore.iron.*
import munit.*
import org.http4s.*
import org.http4s.Method.*
import org.http4s.circe.*
import org.http4s.client.dsl.io.*
import org.http4s.headers.{Accept, `Content-Type`}
import org.scalacheck.effect.PropF
import synonyms.PropHelpers.*
import synonyms.config.Config
import synonyms.domain.*
import synonyms.services.*

class SynonymsRoutesSuite extends CatsEffectSuite with ScalaCheckEffectSuite:
  val cfg = Config.load
  test("GET /synonyms/$word returns JSON response") {
    PropF.forAllF(entryGen) { entry =>
      val req      = GET(Uri.unsafeFromString(s"/synonyms/${entry.word.toString}"))
      val synonyms = testSynonyms(entries = List(entry))
      val routes   = SynonymsRoutes(synonyms, cfg.thesaurusConfig).routes
      routes.run(req).value.flatMap {
        case Some(res) =>
          assertResponse(
            res,
            Status.Ok,
            MediaType.application.json,
            SynonymsByLength.fromEntries(List(entry)).asJson
          )
        case None => IO.pure(fail("No response"))
      }
    }
  }

  test("GET /synonyms/$word returns text response") {
    PropF.forAllF(entryGen) { entry =>
      val req = Request[IO](
        uri = Uri.unsafeFromString(s"/synonyms/${entry.word.toString}"),
        headers = Headers(Accept(MediaRange.`text/*`))
      )
      val synonyms = testSynonyms(entries = List(entry))
      val routes   = SynonymsRoutes(synonyms, cfg.thesaurusConfig).routes
      routes.run(req).value.flatMap {
        case Some(res) =>
          assertResponse(
            res,
            Status.Ok,
            MediaType.text.plain,
            SynonymsByLength.fromEntries(List(entry)).show
          )
        case None => IO.pure(fail("No response"))
      }
    }
  }

  test("GET /synonyms?word=$word1&word=$word2 returns JSON response") {
    PropF.forAllF(resultGen) { case result =>
      val req = GET(
        Uri.unsafeFromString(
          s"/synonyms?word=${result.firstWord}&word=${result.secondWord}"
        )
      )
      val synonyms = testSynonyms(result = result)
      val routes   = SynonymsRoutes(synonyms, cfg.thesaurusConfig).routes
      routes.run(req).value.flatMap {
        case Some(res) =>
          assertResponse(
            res,
            Status.Ok,
            MediaType.application.json,
            result.asJson
          )
        case None => IO.pure(fail("No response"))
      }
    }
  }

  test("GET /synonyms?word=$word1&word=$word2 returns text response") {
    PropF.forAllF(resultGen) { case result =>
      val req = Request[IO](
        uri = Uri.unsafeFromString(
          s"/synonyms?word=${result.firstWord}&word=${result.secondWord}"
        ),
        headers = Headers(Accept(MediaRange.`text/*`))
      )
      val synonyms = testSynonyms(result = result)
      val routes   = SynonymsRoutes(synonyms, cfg.thesaurusConfig).routes
      routes.run(req).value.flatMap {
        case Some(res) =>
          assertResponse(
            res,
            Status.Ok,
            MediaType.text.plain,
            result.show
          )
        case None => IO.pure(fail("No response"))
      }
    }
  }

  def assertResponse[A](
      res: Response[IO],
      expectedStatus: Status,
      expectedContentType: MediaType,
      expectedBody: A
  )(using EntityDecoder[IO, A], Location): IO[Unit] =
    assertEquals(res.status, Status.Ok)
    res.headers.get[`Content-Type`] match
      case None => fail("Could not find Content-Type header")
      case Some(ct) =>
        assertEquals(ct.mediaType, expectedContentType)
    res.as[A].map { json =>
      assertEquals(json, expectedBody)
    }

  def testSynonyms(
      entries: List[Entry] = Nil,
      result: Result = Result.NotSynonyms(Word("foo"), Word("bar"))
  ) = new Synonyms[IO]:
    def getEntries2(word: Word, thesauruses: List[Thesaurus]): IO[List[Entry]] =
      IO.pure(entries)
    def getEntries(word: Word, thesaurus: Thesaurus): IO[List[Entry]] =
      IO.pure(entries)
    def checkSynonyms2(
        first: Word,
        second: Word,
        thesauruses: List[Thesaurus]
    ): IO[Result] = IO.pure(result)
    def checkSynonyms(
        first: Word,
        second: Word,
        thesaurus: Thesaurus
    ): IO[Result] = IO.pure(result)
