package synonyms.model

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._

object MerriamWebster extends Scraper:
  def url(word: String) =  "https://www.merriam-webster.com/thesaurus/$word"
  def synonyms(word: String): List[String] =
    // val doc: Document = browser.get(url(word))
    val doc = browser.parseFile("far.html")
    val entries = doc >> elementList(".thesaurus-entry-container")
    val elements =
      browser.parseFile("far.html") >> elementList(
        ".synonyms_list .thes-word-list-item"
      )

    elements.map(_.text)
