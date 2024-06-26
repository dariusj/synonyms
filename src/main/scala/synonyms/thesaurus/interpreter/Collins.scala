package synonyms.thesaurus.interpreter

import cats.effect.IO
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client.*

object Collins extends JsoupScraper {
  override val name: ThesaurusName = ThesaurusName("Collins")

  def url(word: Word) =
    s"https://www.collinsdictionary.com/dictionary/english-thesaurus/$word"

  override def fetchDocument(word: Word): IO[Either[FetchError, Option[Doc]]] =
    IO(
      // TODO: Use HtmlUnitBrowser for this
      Right(Option(browser.get(url(word))))
    )

  override def buildEntries(word: Word, document: Doc): List[Entry] =
    val entryEls = document >> elementList(".entry")

    entryEls.flatMap { el =>
      val senses = el >> elementList(".sense")

      senses.map { sense =>
        val pos        = el >> text(".headerSensePos")
        val definition = (el >?> text(".def")).orElse(el >?> text(".linkDef"))
        val example    = el >?> text(".type-example")
        val synonyms   = el >> texts(".type-syn .orth")
        Entry(
          name,
          word,
          pos.toPos,
          definition.map(Definition.apply),
          example,
          synonyms.map(Word.apply).toList
        )
      }
    }

  extension (s: String) def toPos: PartOfSpeech = ???
}
