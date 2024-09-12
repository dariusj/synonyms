package synonyms.domain

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.*
import synonyms.domain.*
import synonyms.domain.Result.*

class ThesaurusSuite extends munit.FunSuite:

  entryFixture.test(
    "Entry.hasSynonym returns AreSynonyms when synonym is found"
  ) { entry =>
    assert(clue(clue(entry).hasSynonym(Word("foo"))).isInstanceOf[AreSynonyms])
  }

  entryFixture.test(
    "Entry.hasSynonym returns NotSynonyms when synonym is not found"
  ) { entry =>
    assert(clue(clue(entry).hasSynonym(Word("baz"))).isInstanceOf[NotSynonyms])
  }

  def entryFixture = FunFixture[Entry](
    _ =>
      Entry(
        ThesaurusName("thesaurusName"),
        Word("word"),
        PartOfSpeech.Noun,
        Some(Definition("definition")),
        Some(Example("example")),
        List("foo", "bar").map(Word.apply)
      ),
    _ => ()
  )
