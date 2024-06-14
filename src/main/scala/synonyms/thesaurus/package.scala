package synonyms.thesaurus

import cats.Show

opaque type ThesaurusName = String

object ThesaurusName:
  def apply(value: String): ThesaurusName = value

final case class Entry(
    thesaurusName: ThesaurusName,
    word: String,
    partOfSpeech: String,
    definition: Option[String],
    example: String,
    synonyms: List[String]
):
  def hasSynonym(check: String): Result =
    if synonyms.contains(check) then
      Found(word, check, partOfSpeech, definition, example, thesaurusName)
    else NotFound(word, check)

object Entry:
  def synonymsByLength(entries: List[Entry]): List[(Int, List[String])] =
    entries
      .flatMap(_.synonyms)
      .distinct
      .groupBy(_.filter(Character.isAlphabetic).length)
      .toList
      .map { case (k, v) =>
        k -> v.toList.sorted
      }
      .sortBy(_._1)

sealed abstract class Result:
  def combine(r: Result): Result = (this, r) match
    case (f: Found, _) => f
    case (_, f: Found) => f
    case _             => this

object Result:
  given Show[Result] with
    def show(r: Result): String = r match
      case nf: NotFound =>
        s"${nf.firstWord} and ${nf.secondWord} are not synonyms"
      case f: Found =>
        import f.*
        s"[Source: $source] $firstWord and $secondWord are synonyms - [$partOfSpeech] '${definition
            .getOrElse("No definition given")}': $example"

final case class NotFound(firstWord: String, secondWord: String) extends Result

final case class Found(
    firstWord: String,
    secondWord: String,
    partOfSpeech: String,
    definition: Option[String],
    example: String,
    source: ThesaurusName
) extends Result
