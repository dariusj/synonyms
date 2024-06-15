package synonyms.thesaurus

import cats.Show
import synonyms.thesaurus.Result.*

opaque type ThesaurusName = String

object ThesaurusName:
  def apply(value: String): ThesaurusName = value

final case class Entry(
    thesaurusName: ThesaurusName,
    word: String,
    partOfSpeech: String,
    definition: Option[String],
    example: Option[String],
    synonyms: List[String]
):
  def hasSynonym(check: String): Result =
    if synonyms.contains(check) then
      AreSynonyms(word, check, partOfSpeech, definition, example, thesaurusName)
    else NotSynonyms(word, check)

final case class SynonymsByLength private (length: Int, synonyms: List[String])

object SynonymsByLength:
  given Show[SynonymsByLength] with
    def show(sbl: SynonymsByLength): String =
      s"(${sbl.length}) ${sbl.synonyms.mkString(", ")}"

  def fromEntries(entries: List[Entry]): List[SynonymsByLength] =
    entries
      .flatMap(_.synonyms)
      .distinct
      .groupBy(_.count(Character.isAlphabetic))
      .map { case (k, v) => k -> v.sorted }
      .toList
      .sorted
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

  final case class NotSynonyms(firstWord: String, secondWord: String)
      extends Result

  final case class AreSynonyms(
      firstWord: String,
      secondWord: String,
      partOfSpeech: String,
      definition: Option[String],
      example: Option[String],
      source: ThesaurusName
  ) extends Result
