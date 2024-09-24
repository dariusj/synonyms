package synonyms.domain

import io.github.iltotore.iron.*
import synonyms.domain.*
import synonyms.domain.Result.*

class ThesaurusSuite extends munit.FunSuite:

  entryFixture.test(
    "Entry.hasSynonym returns AreSynonyms when synonym is found"
  ) { entry =>
    clue(entry).hasSynonym(Word("foo")) match
      case _: AreSynonyms => true
      case _: NotSynonyms => fail("Not synonyms")
  }

  entryFixture.test(
    "Entry.hasSynonym returns NotSynonyms when synonym is not found"
  ) { entry =>
    entry.hasSynonym(Word("baz")) match
      case _: AreSynonyms => fail("Are synonyms")
      case _: NotSynonyms => true
  }

  def entryFixture = FunFixture[Entry](
    _ =>
      Entry(
        ThesaurusName("thesaurusName"),
        Word("word"),
        PartOfSpeech.Noun,
        Some(Definition("definition")),
        Some(Example("example")),
        List("foo", "bar").map(Synonym.apply)
      ),
    _ => ()
  )
