package synonyms

final case class EntryItem(
    definition: String,
    example: String,
    synonyms: Vector[String]
)
final case class Entry(
    word: String,
    partOfSpeech: String,
    entryItems: Vector[EntryItem]
)
