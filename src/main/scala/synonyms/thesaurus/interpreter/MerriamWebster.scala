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
      pos.toPos
        .map { p =>
          Entry(
            name,
            word,
            p,
            definition.map(Definition.apply),
            example.map(Example.apply),
            synonyms.map(Word.apply).toList
          )
        }
        .getOrElse(
          throw new RuntimeException(s"Invalid part of speech $pos for $word")
        )

    entryEls.flatMap { entry =>
      val pos = entry >> text(".parts-of-speech")
      (entry >> elementList(".vg-sseq-entry-item")).map(buildEntry(pos))
    }

  extension (s: String)
    def toPos: Option[PartOfSpeech] =
      val pos: PartialFunction[String, PartOfSpeech] =
        case p if p.startsWith("adjective")   => PartOfSpeech.Adjective
        case p if p.startsWith("adverb")      => PartOfSpeech.Adverb
        case p if p.startsWith("noun")        => PartOfSpeech.Noun
        case p if p.startsWith("preposition") => PartOfSpeech.Preposition
        case p if p.startsWith("verb")        => PartOfSpeech.Verb
      pos.lift(s)
