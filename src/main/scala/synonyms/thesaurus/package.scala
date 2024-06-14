package synonyms.thesaurus

import cats.Show

final case class EntryItem(
    definition: String,
    example: String,
    synonyms: List[String]
)
final case class Entry(
    word: String,
    partOfSpeech: String,
    entryItems: List[EntryItem]
):
  def hasSynonym(check: String): Result =
    entryItems
      .collectFirst {
        case item if item.synonyms.contains(check) =>
          Found(word, check, partOfSpeech, item.definition, item.example)
      }
      .getOrElse(NotFound(word, check))

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
        s"$firstWord and $secondWord are synonyms - [$partOfSpeech] '$definition': $example"

final case class NotFound(firstWord: String, secondWord: String) extends Result

final case class Found(
    firstWord: String,
    secondWord: String,
    partOfSpeech: String,
    definition: String,
    example: String
) extends Result
