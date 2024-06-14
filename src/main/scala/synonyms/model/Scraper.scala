package synonyms.model

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document

import net.ruippeixotog.scalascraper.model._

trait Scraper:
  val browser = JsoupBrowser()
  def synonyms(word: String): List[String]
