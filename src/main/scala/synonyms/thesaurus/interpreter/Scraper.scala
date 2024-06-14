package synonyms.thesaurus.interpreter

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import synonyms.thesaurus.algebra.Client

trait Scraper[F[_]] extends Client[F]:
  val browser = JsoupBrowser()
  type Doc = browser.DocumentType
