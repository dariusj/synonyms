package synonyms.core.domain

import cats.data.NonEmptyList
import io.circe.*
import io.circe.generic.semiauto.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import org.http4s.{ParseResult, Uri}

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
  extension (word: Word)
    private def normalise(s: String) =
      s.collect {
        case '-'                  => ' '
        case char if char != '\'' => char
      }
    private[domain] def ===(synonym: Synonym): Boolean =
      normalise(word).equalsIgnoreCase(normalise(synonym))

enum CharacterSet(val counter: Char => Boolean):
  case AnyChar    extends CharacterSet(_ => true)
  case Alphabetic extends CharacterSet(Character.isAlphabetic)

opaque type Synonym = String

object Synonym:
  def apply(value: String): Synonym = value

  given Ordering[Synonym] = Ordering.String
  given Encoder[Synonym]  = Encoder.encodeString.contramap(_.toString)

  extension (synonym: Synonym)
    def countChars(cs: CharacterSet): SynonymLength = SynonymLength(synonym.count(cs.counter))

enum PartOfSpeech:
  case Adjective, Adverb, Conjunction, Determiner, Interjection, Noun, Preposition, Pronoun,
    Undetermined, Verb

object PartOfSpeech:
  given Encoder[PartOfSpeech]  = Encoder.encodeString.contramap(_.toString)
  given Ordering[PartOfSpeech] = Ordering.by(_.toString)

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
  def hasSynonym(otherWord: Word): Result =
    if synonyms.exists(otherWord.===) then
      AreSynonyms(word, otherWord, partOfSpeech, definition, example, thesaurusName)
    else NotSynonyms(word, otherWord)

sealed trait Thesaurus:
  def name: ThesaurusName
  def uri(word: Word): ParseResult[Uri]

object Thesaurus:
  type MerriamWebster = MerriamWebster.type
  case object MerriamWebster extends Thesaurus:
    val name: ThesaurusName = ThesaurusName("Merriam-Webster")
    def uri(word: Word)     = Uri.fromString(s"https://www.merriam-webster.com/thesaurus/$word")

  type Cambridge = Cambridge.type
  case object Cambridge extends Thesaurus:
    val name: ThesaurusName = ThesaurusName("Cambridge")
    def uri(word: Word)     = Uri.fromString(s"https://dictionary.cambridge.org/thesaurus/$word")

  type Collins = Collins.type
  case object Collins extends Thesaurus:
    val name: ThesaurusName = ThesaurusName("Collins")
    def uri(word: Word) = Uri.fromString(
      s"https://www.collinsdictionary.com/dictionary/english-thesaurus/$word"
    )

  type Datamuse = Datamuse.type
  case object Datamuse extends Thesaurus:
    override def name: ThesaurusName = ThesaurusName("Datamuse")
    def uri(word: synonyms.core.domain.Word) =
      Uri.fromString(s"https://api.datamuse.com/words?ml=$word")

    case class Word(word: String, tags: Option[List[String]])

    object Word:
      given Decoder[Word] = deriveDecoder[Word]

  type PowerThesaurus = PowerThesaurus.type
  case object PowerThesaurus extends Thesaurus:
    override val name: ThesaurusName = ThesaurusName("PowerThesaurus")
    override def uri(word: Word)     = Uri.fromString(s"https://powerthesaurus.org/$word/synonyms")

  type WordHippo = WordHippo.type
  case object WordHippo extends Thesaurus:
    val name: ThesaurusName = ThesaurusName("WordHippo")
    def uri(word: Word) = Uri.fromString(
      s"https://www.wordhippo.com/what-is/another-word-for/$word.html"
    )

  def fromString(thesaurusName: String): Option[Thesaurus] =
    val pf: PartialFunction[String, Thesaurus] = {
      case "cambridge"      => Cambridge
      case "datamuse"       => Datamuse
      case "mw"             => MerriamWebster
      case "powerthesaurus" => PowerThesaurus
      case "wordhippo"      => WordHippo
    }
    pf.lift(thesaurusName)

  val all: NonEmptyList[Thesaurus] =
    NonEmptyList.of(Cambridge, Datamuse, MerriamWebster, PowerThesaurus, WordHippo)
