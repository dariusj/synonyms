package synonyms.thesaurus.interpreter

import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import synonyms.thesaurus.*

object Cambridge extends JsoupScraper:
  override val name: ThesaurusName = ThesaurusName("Cambridge")

  def url(word: Word) = s"https://dictionary.cambridge.org/thesaurus/$word"

  override def buildEntries(word: Word, document: Doc): List[Entry] =
    val entryEls = document >> elementList(".entry-block:has(.pos) > div")
    entryEls
      .foldLeft(Option.empty[(String, List[Entry])]) {
        case (acc, el) if el.attr("class").split(" ").contains("lmb-10") =>
          val pos = el >> text(".pos")
          Some(pos, acc.fold(Nil) { case (_, entries) => entries })
        case (Some(pos, entries), el)
            if el.attr("class").split(" ").contains("sense") =>
          val example  = el >?> text(".eg")
          val synonyms = el >> texts(".synonym")

          Some(
            pos,
            entries :+ Entry(
              name,
              word,
              pos.toPos,
              None,
              example.map(Example.apply),
              synonyms.map(Word.apply).toList
            )
          )
        case (acc, _) => acc
      }
      .fold(Nil) { case (_, entries) => entries }

  extension (s: String)
    def toPos: PartOfSpeech = s match
      case "adjective"   => PartOfSpeech.Adjective
      case "adverb"      => PartOfSpeech.Adverb
      case "noun"        => PartOfSpeech.Noun
      case "preposition" => PartOfSpeech.Preposition
      case "verb"        => PartOfSpeech.Verb
