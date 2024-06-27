package synonyms.thesaurus.response

import cats.Show
import synonyms.thesaurus.*

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
    case (f: Result.AreSynonyms, _) => f
    case (_, f: Result.AreSynonyms) => f
    case _                          => this

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
