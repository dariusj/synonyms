package synonyms.thesaurus.interpreter

import cats.effect.IO
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper.model.Element
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Thesaurus

object MerriamWebster extends Thesaurus[IO]:
  def url(word: String) = s"https://www.merriam-webster.com/thesaurus/$word"
  override def fetchDocument(word: String): IO[Document] =
    IO(
      browser.get(url(word))
      // browser.parseFile("far-mw.html")
    )

  override def buildEntries(word: String, document: Document): List[Entry] =
    val entryEls = document >> elementList(".thesaurus-entry-container")

    def buildEntryItem(item: Element) =
      val example = item >> text(".dt span")
      val definition = (item >> text(".dt")).dropRight(example.length + 1)
      val synonyms = item >> texts(
        ".sim-list-scored .synonyms_list li.thes-word-list-item"
      )
      EntryItem(Some(definition), example, synonyms.toList)

    entryEls.map { entry =>
      val partsOfSpeech = entry >> text(".parts-of-speech")
      val items =
        (entry >> elementList(".vg-sseq-entry-item")).map(buildEntryItem)

      Entry(word, partsOfSpeech, items)
    }
