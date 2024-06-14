package synonyms.thesaurus.interpreter

import cats.effect.IO
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Element
import synonyms.thesaurus.*

object MerriamWebster extends Scraper[IO]:
  override val name: ThesaurusName = ThesaurusName("Merriam-Webster")

  def url(word: String) = s"https://www.merriam-webster.com/thesaurus/$word"

  override def fetchDocument(word: String): IO[Doc] = IO(
    browser.get(url(word))
  )

  override def buildEntries(word: String, document: Doc): List[Entry] =
    val entryEls = document >> elementList(".thesaurus-entry-container")

    def buildEntry(pos: String)(el: Element) =
      val example    = el >> text(".dt span")
      val definition = (el >> text(".dt")).dropRight(example.length + 1)
      val synonyms = el >> texts(
        ".sim-list-scored .synonyms_list li.thes-word-list-item"
      )
      Entry(name, word, pos, Some(definition), example, synonyms.toList)

    entryEls.flatMap { entry =>
      val pos = entry >> text(".parts-of-speech")
      (entry >> elementList(".vg-sseq-entry-item")).map(buildEntry(pos))
    }
