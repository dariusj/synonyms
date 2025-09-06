package synonyms.core.domain

import cats.Show
import io.circe.Encoder
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import synonyms.core.domain.*

type SynonymLength = SynonymLength.T

object SynonymLength extends RefinedType[Int, Positive]:
  given Encoder[SynonymLength]  = Encoder.encodeInt.contramap(_.value)
  given Ordering[SynonymLength] = Ordering.Int.on(_.value)
  export math.Ordering.Implicits.infixOrderingOps

case class SynonymsByLength private (length: SynonymLength, synonyms: List[Synonym])

object SynonymsByLength:
  given Encoder[SynonymsByLength] = Encoder.AsObject.derived
  given Show[SynonymsByLength] with
    def show(sbl: SynonymsByLength): String =
      s"(${sbl.length}) ${sbl.synonyms.mkString(", ")}"

  def fromEntries(entries: List[Entry], cs: CharacterSet): List[SynonymsByLength] =
    entries
      .flatMap(_.synonyms)
      .distinct
      .groupBy(_.countChars(cs))
      .map { case (length, synonyms) => length -> synonyms.sorted }
      .toList
      .sortBy { case (length, _) => length }
      .map(SynonymsByLength.apply.tupled)

sealed abstract class Result:
  def firstWord: Word
  def secondWord: Word

  infix def combine(r: Result): Result = (this, r) match
    case (f: Result.AreSynonyms, _) => f
    case (_, f: Result.AreSynonyms) => f
    case _                          => this

object Result:
  given Encoder[Result] = Encoder.AsObject.derived
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
