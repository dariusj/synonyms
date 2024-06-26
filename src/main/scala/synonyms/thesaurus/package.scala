package synonyms.thesaurus

import cats.Show
import synonyms.thesaurus.Result.*

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

case class SynonymsByLength private (length: Int, synonyms: List[Word])

object SynonymsByLength:
  given Show[SynonymsByLength] with
    def show(sbl: SynonymsByLength): String =
      s"(${sbl.length}) ${sbl.synonyms.mkString(", ")}"

  def fromEntries(entries: List[Entry]): List[SynonymsByLength] =
    entries
      .flatMap(_.synonyms)
      .distinct
      .groupBy(_.countChars(Character.isAlphabetic))
      .map { case (length, synonyms) => length -> synonyms.sorted }
      .toList
      .sortBy { case (length, _) => length }
      .map(SynonymsByLength.apply.tupled)

sealed abstract class Result:
  infix def combine(r: Result): Result = (this, r) match
    case (f: AreSynonyms, _) => f
    case (_, f: AreSynonyms) => f
    case _                   => this

object Result:
  given Show[Result] with
    def show(r: Result): String = r match
      case nf: NotSynonyms =>
        s"${nf.firstWord} and ${nf.secondWord} are not synonyms"
      case f: AreSynonyms =>
        import f.*
        s"[Source: $source] $firstWord and $secondWord are synonyms - [$partOfSpeech] '${definition
            .getOrElse("No definition given")}': ${example.getOrElse("No example given")}"

  case class NotSynonyms(firstWord: Word, secondWord: Word) extends Result

  case class AreSynonyms(
      firstWord: Word,
      secondWord: Word,
      partOfSpeech: PartOfSpeech,
      definition: Option[Definition],
      example: Option[Example],
      source: ThesaurusName
  ) extends Result
