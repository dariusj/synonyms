package synonyms.thesaurus

import synonyms.thesaurus.response.Result
import synonyms.thesaurus.response.Result.*

opaque type ThesaurusName = String

object ThesaurusName:
  def apply(value: String): ThesaurusName = value

opaque type Word = String

object Word:
  def apply(value: String): Word = value

  private val orderingString = summon[Ordering[String]]
  given Ordering[Word]       = orderingString

  extension (w: Word)
    // Calling this something other than the underlying method works
    // around https://github.com/scala/scala3/issues/10947
    def countChars(p: Char => Boolean): Int = w.toString.count(p)

enum PartOfSpeech:
  case Adjective, Adverb, Noun, Preposition, Verb

opaque type Definition = String

object Definition:
  def apply(value: String): Definition = value

opaque type Example = String

object Example:
  def apply(value: String): Example = value

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
