package synonyms

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper.model.Element

trait Thesaurus[F[_]]:
  val browser = JsoupBrowser()

  def fetchDocument(word: String): F[Document]
  def buildEntries(word: String, document: Document): List[Entry]
