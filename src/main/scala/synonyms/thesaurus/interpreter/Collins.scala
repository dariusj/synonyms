package synonyms.thesaurus.interpreter

import cats.effect.IO
import cats.syntax.option.*
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Document
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Thesaurus

object Collins extends Thesaurus[IO] {
  def url(word: String) =
    s"https://www.collinsdictionary.com/dictionary/english-thesaurus/$word"

  override val name: ThesaurusName = ThesaurusName("Collins")

  override def fetchDocument(word: String): IO[Document] = IO(
    // TODO: Use HtmlUnitBrowser for this
    browser.get(url(word))
  )

  override def buildEntries(word: String, document: Document): List[Entry] =
    val entryEls = document >> elementList(".entry")

    entryEls.flatMap { el =>
      val senses = el >> elementList(".sense")

      senses.map { sense =>
        val pos        = el >> text(".headerSensePos")
        val definition = (el >?> text(".def")).orElse(el >?> text(".linkDef"))
        val example    = el >?> text(".type-example")
        val synonyms   = el >> texts(".type-syn .orth")
        Entry(name, word, pos, definition, example.orEmpty, synonyms.toList)
      }
    }
}
