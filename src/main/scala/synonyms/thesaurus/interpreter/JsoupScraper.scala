package synonyms.thesaurus.interpreter

import cats.Functor
import cats.effect.Sync
import cats.syntax.applicativeError.*
import cats.syntax.either.*
import cats.syntax.functor.*
import cats.syntax.option.*
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.jsoup.HttpStatusException
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.algebra.Client.FetchError

trait JsoupScraper[F[_]: Sync: Functor] extends Client[F]:
  val browser: Browser = JsoupBrowser()
  type Doc = browser.DocumentType

  def url(word: Word): String

  override def fetchDocument(word: Word): F[Either[FetchError, Option[Doc]]] =
    Sync[F].delay(browser.get(url(word))).map(_.some.asRight).recover {
      case e: HttpStatusException if e.getStatusCode == 404 => None.asRight
    }
