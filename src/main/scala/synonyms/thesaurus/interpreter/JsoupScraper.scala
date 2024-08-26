package synonyms.thesaurus.interpreter

import cats.effect.IO
import cats.syntax.either.*
import cats.syntax.option.*
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
    IO(browser.get(url(word))).map(_.some.asRight).recover {
      case e: HttpStatusException if e.getStatusCode == 404 => None.asRight
    }
