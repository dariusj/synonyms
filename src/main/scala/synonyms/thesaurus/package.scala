package synonyms.thesaurus

final case class EntryItem(definition: String, synonyms: Vector[String])
final case class Entry(
    word: String,
    partOfSpeech: String,
    entryItems: Vector[EntryItem]
)
