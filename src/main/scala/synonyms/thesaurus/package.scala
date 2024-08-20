package synonyms.thesaurus

import io.circe.{Encoder, Json}
import synonyms.thesaurus.response.Result
import synonyms.thesaurus.response.Result.*

opaque type ThesaurusName = String

object ThesaurusName:
  def apply(value: String): ThesaurusName = value
  // Using contramap/Encode[String].apply results in future/stack overflows respectively
  given Encoder[ThesaurusName] with
    override def apply(a: ThesaurusName): Json = Json.fromString(a)

opaque type Word = String

object Word:
  def apply(value: String): Word = value

  private val orderingString = summon[Ordering[String]]
  given Ordering[Word]       = orderingString

  extension (w: Word)
    // Calling this something other than the underlying method works
    // around https://github.com/scala/scala3/issues/10947
    def countChars(p: Char => Boolean): Int = w.toString.count(p)

  given Encoder[Word] with
    override def apply(a: Word): Json = Json.fromString(a)

enum PartOfSpeech:
  case Adjective, Adverb, Noun, Preposition, Undetermined, Verb

opaque type Definition = String

object Definition:
  def apply(value: String): Definition = value
  given Encoder[Definition] with
    override def apply(a: Definition): Json = Json.fromString(a)

opaque type Example = String

object Example:
  def apply(value: String): Example = value
  given Encoder[Example] with
    override def apply(a: Example): Json = Json.fromString(a)

case class Entry(
    thesaurusName: ThesaurusName,
    word: Word,
    partOfSpeech: PartOfSpeech,
    definition: Option[Definition],
    example: Option[Example],
    synonyms: List[Word]
):
  def hasSynonym(check: Word): Result =
    if synonyms.contains(check) then
      AreSynonyms(word, check, partOfSpeech, definition, example, thesaurusName)
    else NotSynonyms(word, check)
