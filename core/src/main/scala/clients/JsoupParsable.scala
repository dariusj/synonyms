package synonyms.core.clients

import cats.syntax.either.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.syntax.traverse.*
import cats.{Applicative, Monad, MonadThrow}
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.dsl.ToQuery
import net.ruippeixotog.scalascraper.model.{Document, Element}
import synonyms.core.clients.ParseException.*
import synonyms.core.domain.*
import synonyms.core.domain.Thesaurus.*

trait JsoupParsable[F[_], T]:
  def parseDocument(word: Word, document: Document): F[List[Entry]]

object JsoupParsable:
  extension (el: Element)
    def hasClass(name: String): Boolean =
      // We need to be defensive as 'attr' throws if the attribute isn't defined
      Option.when(el.hasAttr("class"))(el.attr("class")).exists(_.split(" ").contains(name))
    def hasId(name: String): Boolean =
      Option.when(el.hasAttr("id"))(el.attr("id")).exists(_.split(" ").contains(name))

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
        case "pronoun"     => PartOfSpeech.Pronoun
        case "verb"        => PartOfSpeech.Verb

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

  given [F[_]: MonadThrow]: JsoupParsable[F, WordHippo] with
    given ThesaurusName = WordHippo.name
    extension (s: String)
      def toPos: PartOfSpeech = s match
        case "Adjective"    => PartOfSpeech.Adjective
        case "Adverb"       => PartOfSpeech.Adverb
        case "Conjunction"  => PartOfSpeech.Conjunction
        case "Determiner"   => PartOfSpeech.Determiner
        case "Interjection" => PartOfSpeech.Interjection
        case "Noun"         => PartOfSpeech.Noun
        case "Preposition"  => PartOfSpeech.Preposition
        case "Pronoun"      => PartOfSpeech.Pronoun
        case "Verb"         => PartOfSpeech.Verb

    def parseDocument(word: Word, document: Document): F[List[Entry]] =
      Applicative[F]
        .pure(document)
        .map(_ >> elementList("td#contentpagecell td > div"))
        .map { elements =>
          elements.iterator
            .takeWhile(!_.hasId("searchagaincontainer"))
            .foldLeft(Vector.empty[Entry]) {
              case (entries, el) if el.hasClass("wordtype") =>
                entries :+ Entry(
                  WordHippo.name,
                  word,
                  // We drop an optional `▲` from the end
                  el.text.takeWhile(Character.isAlphabetic).toPos,
                  None,
                  None,
                  Nil
                )
              case (entries, el) if el.hasClass("tabdesc") =>
                entries.init :+ entries.last.copy(definition = Option(Definition(el.text)))
              case (entries, el) if el.hasClass("relatedwords") && el.select(".wb").nonEmpty =>
                entries.init :+ entries.last
                  .copy(synonyms = (el >> texts(".wb")).map(Synonym.apply).toList)
              case (entries, el) if el.hasClass("tabexample") =>
                entries.init :+ entries.last
                  .copy(example = Option(Example(el.text.drop(1).dropRight(1))))
              case (entries, el) => entries
            }
            .toList
        }

  // PowerThesaurus has an infinite scroll feature to get more synonyms, but we can't trigger that
  // with Jsoup
  given [F[_]: Monad]: JsoupParsable[F, PowerThesaurus] with
    given ThesaurusName = PowerThesaurus.name
    extension (s: String)
      def toPos: PartOfSpeech =
        s match
          case "adj."  => PartOfSpeech.Adjective
          case "adv."  => PartOfSpeech.Adverb
          case "conj." => PartOfSpeech.Conjunction
          case "int."  => PartOfSpeech.Interjection
          case "n."    => PartOfSpeech.Noun
          case "prep." => PartOfSpeech.Preposition
          case "v."    => PartOfSpeech.Verb

    def parseDocument(word: Word, document: Document): F[List[Entry]] =
      def extractPos(el: Element): Iterable[PartOfSpeech] =
        // This catches any pos before an optional "#".
        // Being explicit with the classes excludes a ", "
        val secondaryArea = el >> texts("#secondary-area .r3_e5 > .ct_a8, .zk_zl")
        val partsOfSpeech = secondaryArea.takeWhile(_ != "#").map(_.toPos)
        partsOfSpeech.headOption.as(partsOfSpeech).getOrElse(Iterable(PartOfSpeech.Undetermined))

      Applicative[F]
        .pure(document)
        .map(_ >> elementList(".r3_b"))
        .map(_.flatMap { el =>
          val synonym = el >> text("#primary-area")
          extractPos(el).map(_ -> Synonym(synonym))
        })
        .map(
          _.groupBy { case (pos, _) => pos }
            .map { case (pos, posSynonyms) =>
              Entry(
                PowerThesaurus.name,
                word,
                pos,
                None,
                None,
                posSynonyms.map { case (_, synonyms) => synonyms }
              )
            }
            .toList
            .sortBy(_.partOfSpeech)
        )
