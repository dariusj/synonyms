package synonyms.thesaurus.interpreter

import cats.effect.IO
import cats.syntax.either.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.jsoup.HttpStatusException
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.algebra.Client.ClientError
import synonyms.thesaurus.algebra.Client.NotFound

trait JsoupScraper extends Client[IO]:
  val browser = JsoupBrowser()
  type Doc = browser.DocumentType

  def url(word: String): String

  override def fetchDocument(word: String): IO[Either[ClientError, Doc]] =
    IO(browser.get(url(word))).attempt.flatMap {
      case Right(v) => IO.pure(v.asRight)
      case Left(e: HttpStatusException) if e.getStatusCode == 404 =>
        IO.pure(NotFound(word, e.getUrl).asLeft[Doc])
      case Left(e) => IO.raiseError(e)
    }
