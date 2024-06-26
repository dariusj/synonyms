package synonyms.thesaurus.interpreter

import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Element
import synonyms.thesaurus.*

object MerriamWebster extends JsoupScraper:
  override val name: ThesaurusName = ThesaurusName("Merriam-Webster")

  def url(word: Word) = s"https://www.merriam-webster.com/thesaurus/$word"

  override def buildEntries(word: Word, document: Doc): List[Entry] =
    val entryEls = document >> elementList(".thesaurus-entry-container")

    def buildEntry(pos: String)(el: Element) =
      val example = el >?> text(".dt span")
      val definition =
        example.map(ex => (el >> text(".dt")).dropRight(ex.length + 1))
      val synonyms = el >> texts(
        ".sim-list-scored .synonyms_list li.thes-word-list-item"
      )
      Entry(
        name,
        word,
        pos.toPos,
        definition.map(Definition.apply),
        example.map(Example.apply),
        synonyms.map(Word.apply).toList
      )

    entryEls.flatMap { entry =>
      val pos = entry >> text(".parts-of-speech")
      (entry >> elementList(".vg-sseq-entry-item")).map(buildEntry(pos))
    }

  extension (s: String)
    def toPos: PartOfSpeech = s match
      case "adjective"   => PartOfSpeech.Adjective
      case "adverb"      => PartOfSpeech.Adverb
      case "noun"        => PartOfSpeech.Noun
      case "preposition" => PartOfSpeech.Preposition
      case "verb"        => PartOfSpeech.Verb
