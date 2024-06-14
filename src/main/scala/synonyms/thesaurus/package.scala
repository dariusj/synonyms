package synonyms.thesaurus

import cats.Show

final case class EntryItem(
    definition: String,
    example: String,
    synonyms: Vector[String]
)
final case class Entry(
    word: String,
    partOfSpeech: String,
    entryItems: Vector[EntryItem]
):
  def synonym(check: String): Option[Result] =
    entryItems.collectFirst {
      case item if item.synonyms.contains(check) =>
        Result(word, check, partOfSpeech, item.definition, item.example)
    }

final case class Result(
    firstWord: String,
    secondWord: String,
    partOfSpeech: String,
    definition: String,
    example: String
)

object Result:
  given Show[Result] with
    def show(t: Result): String =
      import t.*
      s"[$partOfSpeech] $firstWord and $secondWord are synonyms - '$definition': $example"
