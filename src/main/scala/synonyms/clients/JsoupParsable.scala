package synonyms.clients

import cats.Applicative
import cats.Monad
import cats.MonadThrow
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.traverse.*
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.dsl.ToQuery
import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper.model.Element
import synonyms.domain.*
import synonyms.domain.Thesaurus.*

trait JsoupParsable[F[_], T]:
  def parseDocument(word: Word, document: Document): F[List[Entry]]

object JsoupParsable:
  given [F[_]: MonadThrow]: JsoupParsable[F, MerriamWebster] with
    extension (s: String)
      def toPos: Option[PartOfSpeech] =
        val pos: PartialFunction[String, PartOfSpeech] =
          case p if p.startsWith("adjective")   => PartOfSpeech.Adjective
          case p if p.startsWith("adverb")      => PartOfSpeech.Adverb
          case p if p.startsWith("noun")        => PartOfSpeech.Noun
          case p if p.startsWith("preposition") => PartOfSpeech.Preposition
          case p if p.startsWith("verb")        => PartOfSpeech.Verb
        pos.lift(s)

    def parseDocument(word: Word, document: Document): F[List[Entry]] =
      def buildEntry(pos: PartOfSpeech)(el: Element) =
        val example = el >?> text(".dt span")
        val definition =
          example.map(ex => (el >> text(".dt")).dropRight(ex.length + 1))
        val synonyms = el >> texts(
          ".sim-list-scored .synonyms_list li.thes-word-list-item"
        )
        Entry(
          MerriamWebster.name,
          word,
          pos,
          definition.map(Definition.apply),
          example.map(Example.apply),
          synonyms.map(Word.apply).toList
        )

      Applicative[F]
        .pure(document)
        .map(_ >> elementList(".thesaurus-entry-container"))
        .flatMap { entryEls =>
          entryEls.flatTraverse { entry =>
            val posString = entry >> text(".parts-of-speech")
            posString.toPos match
              // TODO: Add Test
              case None =>
                MonadThrow[F].raiseError(
                  ParseException
                    .PartOfSpeechNotFound(posString, word, MerriamWebster.name)
                )
              case Some(pos) =>
                Applicative[F]
                  .pure(entry)
                  .map(_ >> elementList(".vg-sseq-entry-item"))
                  .map(_.map(buildEntry(pos)))
          }
        }

  given [F[_]: Monad]: JsoupParsable[F, Cambridge] with
    extension (s: String)
      def toPos: PartOfSpeech = s match
        case "adjective"   => PartOfSpeech.Adjective
        case "adverb"      => PartOfSpeech.Adverb
        case "noun"        => PartOfSpeech.Noun
        case "preposition" => PartOfSpeech.Preposition
        case "verb"        => PartOfSpeech.Verb

    def parseDocument(word: Word, document: Document): F[List[Entry]] =
      Applicative[F]
        .pure(document)
        .map(_ >> elementList(".entry-block:has(.pos) > div"))
        .map { entryEls =>
          entryEls
            .foldLeft(Option.empty[(String, List[Entry])]) {
              case (acc, el) if el.attr("class").split(" ").contains("lmb-10") =>
                val pos = el >> text(".pos")
                Some(pos, acc.fold(Nil) { case (_, entries) => entries })
              case (Some(pos, entries), el) if el.attr("class").split(" ").contains("sense") =>
                val example  = el >?> text(".eg")
                val synonyms = el >> texts(".synonym")

                val entry = Entry(
                  Cambridge.name,
                  word,
                  pos.toPos,
                  None,
                  example.map(Example.apply),
                  synonyms.map(Word.apply).toList
                )
                Some(pos, entries :+ entry)
              case (acc, _) => acc
            }
            .fold(Nil) { case (_, entries) => entries }
        }

// TODO: Move to HtmlUnitBrowser
// given [F[_]: Monad]: JsoupParsable[F, Collins] with
//   val thesaurus                                 = Thesaurus.Collins
//   extension (s: String) def toPos: PartOfSpeech = ???
//   def parseDocument(word: Word, document: Document): F[List[Entry]] =
//     Applicative[F].pure(document).map(_ >> elementList(".entry")).map {
//       entryEls =>
//         entryEls.flatMap { el =>
//           val senses = el >> elementList(".sense")

//           senses.map { sense =>
//             val pos = el >> text(".headerSensePos")
//             val definition =
//               (el >?> text(".def")).orElse(el >?> text(".linkDef"))
//             val example  = el >?> text(".type-example")
//             val synonyms = el >> texts(".type-syn .orth")
//             Entry(
//               thesaurus.name,
//               word,
//               pos.toPos,
//               definition.map(Definition.apply),
//               example.map(Example.apply),
//               synonyms.map(Word.apply).toList
//             )
//           }
//         }
//     }
