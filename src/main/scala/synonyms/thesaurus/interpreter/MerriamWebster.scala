package synonyms.thesaurus.interpreter

import cats.MonadThrow
import cats.effect.Sync
import cats.syntax.flatMap.*
import cats.syntax.traverse.*
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Element
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client.ParseException.PartOfSpeechNotFound

class MerriamWebster[F[_]: Sync] extends JsoupScraper[F]:
  override val name: ThesaurusName = ThesaurusName("Merriam-Webster")

  def url(word: Word) = s"https://www.merriam-webster.com/thesaurus/$word"

  override def buildEntries(word: Word, document: Doc): F[List[Entry]] =
    def buildEntry(pos: PartOfSpeech)(el: Element) =
      val example = el >?> text(".dt span")
      val definition =
        example.map(ex => (el >> text(".dt")).dropRight(ex.length + 1))
      val synonyms = el >> texts(
        ".sim-list-scored .synonyms_list li.thes-word-list-item"
      )
      Entry(
        name,
        word,
        pos,
        definition.map(Definition.apply),
        example.map(Example.apply),
        synonyms.map(Word.apply).toList
      )

    Sync[F]
      .delay(document >> elementList(".thesaurus-entry-container"))
      .flatMap { entryEls =>
        entryEls.flatTraverse { entry =>
          val posString = entry >> text(".parts-of-speech")
          posString.toPos match
            // TODO: Add Test
            case None =>
              MonadThrow[F].raiseError(
                PartOfSpeechNotFound(posString, word, name)
              )
            case Some(pos) =>
              Sync[F].delay(
                (entry >> elementList(".vg-sseq-entry-item")).map(
                  buildEntry(pos)
                )
              )
        }
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
