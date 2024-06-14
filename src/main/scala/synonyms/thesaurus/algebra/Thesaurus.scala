package synonyms.thesaurus.algebra

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import synonyms.thesaurus.*

trait Thesaurus[F[_]]:
  val browser = JsoupBrowser()

  def name: ThesaurusName

  def fetchDocument(word: String): F[Document]
  def buildEntries(word: String, document: Document): List[Entry]
