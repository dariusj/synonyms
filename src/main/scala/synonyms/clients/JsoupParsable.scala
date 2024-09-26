package synonyms.clients

import cats.syntax.either.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.traverse.*
import cats.{Applicative, MonadThrow}
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.dsl.ToQuery
import net.ruippeixotog.scalascraper.model.{Document, Element}
import synonyms.clients.ParseException.*
import synonyms.domain.*
import synonyms.domain.Thesaurus.*

trait JsoupParsable[F[_], T]:
  def parseDocument(word: Word, document: Document): F[List[Entry]]

object JsoupParsable:
  given [F[_]: MonadThrow]: JsoupParsable[F, MerriamWebster] with
    given ThesaurusName = MerriamWebster.name
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
          synonyms.map(Synonym.apply).toList
        )

      Applicative[F]
        .pure(document)
        .map(_ >> elementList(".thesaurus-entry-container"))
        .flatMap { entryEls =>
          entryEls.flatTraverse { entry =>
            val posString = entry >> text(".parts-of-speech")
            posString.toPos match
              // TODO: Add Test
              case None => MonadThrow[F].raiseError(PartOfSpeechNotFound(posString, word))
              case Some(pos) =>
                Applicative[F]
                  .pure(entry)
                  .map(_ >> elementList(".vg-sseq-entry-item"))
                  .map(_.map(buildEntry(pos)))
          }
        }

  given [F[_]: MonadThrow]: JsoupParsable[F, Cambridge] with
    given ThesaurusName = Cambridge.name
    extension (s: String)
      // TODO: FIXME - unsafe
      def toPos: PartOfSpeech = s match
        case "adjective"   => PartOfSpeech.Adjective
        case "adverb"      => PartOfSpeech.Adverb
        case "noun"        => PartOfSpeech.Noun
        case "preposition" => PartOfSpeech.Preposition
        case "verb"        => PartOfSpeech.Verb

    extension (el: Element)
      def hasClass(name: String): Boolean = el.attr("class").split(" ").contains(name)

    private case class Acc(currentPos: Option[PartOfSpeech], entries: List[Entry]):
      def handlePosEl(el: Element): Acc =
        val pos = el >> text(".pos")
        Acc(Some(pos.toPos), entries)

      def handleSynonymsEl(el: Element, word: Word): Either[ParseException, Acc] =
        currentPos.toRight(EntryWithoutPos(word, Cambridge.name)).map { pos =>
          val example  = el >?> text(".eg")
          val synonyms = el >> texts(".synonym")
          val entry = Entry(
            Cambridge.name,
            word,
            pos,
            None,
            example.map(Example.apply),
            synonyms.map(Synonym.apply).toList
          )
          Acc(currentPos, entries :+ entry)
        }
      def combineWithElement(element: Element, word: Word): Either[ParseException, Acc] =
        element match
          case el if el.hasClass("lmb-10") => handlePosEl(el).asRight
          case el if el.hasClass("sense")  => handleSynonymsEl(el, word)
          case _                           => this.asRight

    private object Acc:
      def empty: Acc = Acc(None, Nil)

    def parseDocument(word: Word, document: Document): F[List[Entry]] =
      Applicative[F]
        .pure(document)
        .map(_ >> elementList(".entry-block:has(.pos) > div"))
        .map { entryEls =>
          entryEls
            .foldLeft(Acc.empty.asRight[ParseException]) {
              case (Right(acc), el) => acc.combineWithElement(el, word)
              case (acc, _)         => acc
            }
            .map(_.entries)
        }
        .flatMap(_.liftTo[F])