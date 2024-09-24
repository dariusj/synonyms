package synonyms.domain

import cats.data.NonEmptyList
import io.circe.*
import io.circe.generic.semiauto.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

import Result.*

opaque type ThesaurusName = String

object ThesaurusName:
  def apply(value: String): ThesaurusName = value
  // Using contramap/Encode[String].apply results in future/stack overflows respectively
  given Encoder[ThesaurusName] with
    override def apply(a: ThesaurusName): Json = Json.fromString(a)

opaque type Word = String :| (Not[Blank] & Not[Digit])

object Word extends RefinedTypeOps[String, Not[Blank] & Not[Exists[Digit]], Word]:
  given Encoder[Word] with
    override def apply(a: Word): Json = Json.fromString(a.value)

opaque type Synonym = String

object Synonym:
  def apply(value: String): Synonym = value

  given Ordering[Synonym] with
    def compare(x: Synonym, y: Synonym): Int = x.compareTo(y)
  given Encoder[Synonym] = Encoder.encodeString.contramap(_.toString)

  extension (w: Synonym) def countChars(p: Char => Boolean): Int = w.count(p)

enum PartOfSpeech:
  case Adjective, Adverb, Noun, Preposition, Undetermined, Verb

opaque type Definition = String

object Definition:
  def apply(value: String): Definition = value
  given Encoder[Definition]            = Encoder.encodeString.contramap(_.toString)

opaque type Example = String

object Example:
  def apply(value: String): Example = value
  given Encoder[Example]            = Encoder.encodeString.contramap(_.toString)

case class Entry(
    thesaurusName: ThesaurusName,
    word: Word,
    partOfSpeech: PartOfSpeech,
    definition: Option[Definition],
    example: Option[Example],
    synonyms: List[Synonym]
):
  def hasSynonym(check: Word): Result =
    if synonyms.contains(check) then
      AreSynonyms(word, check, partOfSpeech, definition, example, thesaurusName)
    else NotSynonyms(word, check)

sealed trait Thesaurus:
  def name: ThesaurusName
  def url(word: Word): String

object Thesaurus:
  type MerriamWebster = MerriamWebster.type
  case object MerriamWebster extends Thesaurus:
    val name: ThesaurusName = ThesaurusName("Merriam-Webster")
    def url(word: Word)     = s"https://www.merriam-webster.com/thesaurus/$word"

  type Cambridge = Cambridge.type
  case object Cambridge extends Thesaurus:
    val name: ThesaurusName = ThesaurusName("Cambridge")
    def url(word: Word)     = s"https://dictionary.cambridge.org/thesaurus/$word"

  type Collins = Collins.type
  case object Collins extends Thesaurus:
    val name: ThesaurusName = ThesaurusName("Collins")
    def url(word: Word) =
      s"https://www.collinsdictionary.com/dictionary/english-thesaurus/$word"

  type Datamuse = Datamuse.type
  case object Datamuse extends Thesaurus:
    override def name: ThesaurusName = ThesaurusName("Datamuse")
    def url(word: _root_.synonyms.domain.Word) =
      s"https://api.datamuse.com/words?ml=$word"

    case class Word(word: String, tags: Option[List[String]])

    object Word:
      given Decoder[Word] = deriveDecoder[Word]

  def fromString(thesaurusName: String): Option[Thesaurus] =
    val pf: PartialFunction[String, Thesaurus] = {
      case "mw"        => MerriamWebster
      case "cambridge" => Cambridge
    }
    pf.lift(thesaurusName)

  val all: NonEmptyList[Thesaurus] =
    NonEmptyList.of(MerriamWebster, Cambridge, Datamuse)
