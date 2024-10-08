package synonyms.http.routes

import cats.effect.*
import munit.*
import org.http4s.*
import org.http4s.Method.*
import org.http4s.client.dsl.io.*
import org.http4s.headers.{Accept, `Content-Type`}
import org.scalacheck.effect.PropF
import synonyms.core.PropHelpers.*
import synonyms.core.config.Config
import synonyms.core.domain.*
import synonyms.core.programs.*
import synonyms.core.services.*

class SynonymsRoutesSuite extends CatsEffectSuite with ScalaCheckEffectSuite:

  override def munitFixtures = List(configFixture)

  test("GET /synonyms/$word returns JSON response") {
    PropF.forAllF(entryGen) { entry =>
      configFixture().flatMap { cfg =>
        val req      = GET(Uri.unsafeFromString(s"/synonyms/${entry.word.toString}"))
        val service  = testThesaurusService(entries = List(entry))
        val synonyms = Synonyms(service)
        val routes   = SynonymsRoutes(synonyms, cfg.thesaurusConfig).routes

        routes.run(req).value.map {
          case Some(res) => assertResponse(res, Status.Ok, MediaType.application.json)
          case None      => fail("No response")
        }
      }
    }
  }

  test("GET /synonyms/$word returns text response") {
    PropF.forAllF(entryGen) { entry =>
      configFixture().flatMap { cfg =>
        val req = Request[IO](
          uri = Uri.unsafeFromString(s"/synonyms/${entry.word.toString}"),
          headers = Headers(Accept(MediaRange.`text/*`))
        )
        val service  = testThesaurusService(entries = List(entry))
        val synonyms = Synonyms(service)
        val routes   = SynonymsRoutes(synonyms, cfg.thesaurusConfig).routes

        routes.run(req).value.map {
          case Some(res) => assertResponse(res, Status.Ok, MediaType.text.plain)
          case None      => fail("No response")
        }
      }
    }
  }

  test("GET /synonyms?word=$word1&word=$word2 returns JSON response") {
    PropF.forAllF(resultGen) { case result =>
      configFixture().flatMap { cfg =>
        val req = GET(
          Uri.unsafeFromString(
            s"/synonyms?word=${result.firstWord}&word=${result.secondWord}"
          )
        )
        val service  = testThesaurusService()
        val synonyms = Synonyms(service)
        val routes   = SynonymsRoutes(synonyms, cfg.thesaurusConfig).routes
        routes.run(req).value.map {
          case Some(res) => assertResponse(res, Status.Ok, MediaType.application.json)
          case None      => fail("No response")
        }
      }
    }
  }

  test("GET /synonyms?word=$word1&word=$word2 returns text response") {
    PropF.forAllF(resultGen) { case result =>
      configFixture().flatMap { cfg =>
        val req = Request[IO](
          uri = Uri.unsafeFromString(
            s"/synonyms?word=${result.firstWord}&word=${result.secondWord}"
          ),
          headers = Headers(Accept(MediaRange.`text/*`))
        )
        val service  = testThesaurusService()
        val synonyms = Synonyms(service)
        val routes   = SynonymsRoutes(synonyms, cfg.thesaurusConfig).routes
        routes.run(req).value.map {
          case Some(res) => assertResponse(res, Status.Ok, MediaType.text.plain)
          case None      => fail("No response")
        }
      }
    }
  }

  val configFixture = ResourceSuiteLocalFixture("config", Resource.pure(Config.load[IO]))

  def assertResponse(res: Response[IO], expectedStatus: Status, expectedContentType: MediaType)(
      using Location
  ): Unit =
    assertEquals(res.status, Status.Ok)
    res.headers.get[`Content-Type`] match
      case None     => fail("Could not find Content-Type header")
      case Some(ct) => assertEquals(ct.mediaType, expectedContentType)

  def testThesaurusService(entries: List[Entry] = Nil) = new ThesaurusService[IO]:
    def getEntries(word: Word, thesaurus: Thesaurus): IO[List[Entry]] = IO.pure(entries)
