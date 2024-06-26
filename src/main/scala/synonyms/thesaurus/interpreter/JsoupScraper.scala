package synonyms.thesaurus.interpreter

import cats.effect.IO
import cats.syntax.either.*
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.jsoup.HttpStatusException
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.algebra.Client.FetchError

trait JsoupScraper extends Client[IO]:
  val browser: Browser = JsoupBrowser()
  type Doc = browser.DocumentType

  def url(word: Word): String

  override def fetchDocument(word: Word): IO[Either[FetchError, Option[Doc]]] =
    IO(browser.get(url(word))).attempt.flatMap {
      case Right(v) => IO.pure(Some(v).asRight)
      case Left(e: HttpStatusException) if e.getStatusCode == 404 =>
        IO.pure(None.asRight)
      case Left(e) => IO.raiseError(e)
    }
