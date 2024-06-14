package synonyms.thesaurus.interpreter

import cats.effect.IO
import cats.syntax.option.*
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper.model.Element
import synonyms.thesaurus.Entry
import synonyms.thesaurus.EntryItem
import synonyms.thesaurus.algebra.Thesaurus

object Collins extends Thesaurus[IO] {
  def url(word: String) =
    s"https://www.collinsdictionary.com/dictionary/english-thesaurus/$word"

  override def fetchDocument(word: String): IO[Document] = IO(
    // TODO: Use HtmlUnitBrowser for this
    browser.get(url(word))
  )

  override def buildEntries(word: String, document: Document): List[Entry] =
    val entryEls = document >> elementList(".entry")

    entryEls.flatMap { el =>
      val senses = el >> elementList(".sense")

      def buildEntryItem(el: Element) =
        val definition = (el >?> text(".def")).orElse(el >?> text(".linkDef"))
        val example = el >?> text(".type-example")
        val synonyms = el >> texts(".type-syn .orth")
        EntryItem(definition, example.orEmpty, synonyms.toList)

      val data = senses.foldLeft(List.empty[(String, EntryItem)]) {
        case (acc, sense) =>
          val pos = el >> text(".headerSensePos")
          acc :+ pos -> buildEntryItem(sense)
      }
      data.groupMap(_._1)(_._2).foldLeft(List.empty[Entry]) {
        case (acc, (pos, entryItems)) => acc :+ Entry(word, pos, entryItems)
      }
    }
}
