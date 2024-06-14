package synonyms.thesaurus.interpreter

import cats.effect.IO
import monocle.syntax.all.*
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Document
import synonyms.thesaurus.Entry
import synonyms.thesaurus.EntryItem
import synonyms.thesaurus.algebra.Thesaurus

object Cambridge extends Thesaurus[IO]:
  def url(word: String) = s"https://dictionary.cambridge.org/thesaurus/$word"

  override def fetchDocument(word: String): IO[Document] = IO(
    browser.get(url(word))
    // browser.parseFile("run-cambridge.html")
  )

  override def buildEntries(word: String, document: Document): List[Entry] =
    val entryEls = (document >> elementList(".entry-block:has(.pos) > div"))
    entryEls
      .foldLeft(List.empty[Entry]) {
        case (acc, el) if el.attr("class").split(" ").contains("lmb-10") =>
          val entry = Entry(word, el >> text(".pos"), Nil)
          entry +: acc
        case (acc, el) if el.attr("class").split(" ").contains("sense") =>
          val example = el >> text(".eg")
          val synonyms = el >> texts(".synonym")

          acc.updated(
            0,
            acc.head
              .focus(_.entryItems)
              .modify(EntryItem(None, example, synonyms.toList) +: _)
          )
        case (acc, _) => acc
      }
      .toList
