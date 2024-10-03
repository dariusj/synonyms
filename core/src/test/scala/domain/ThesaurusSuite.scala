package synonyms.core.domain

import io.github.iltotore.iron.*
import synonyms.core.domain.*
import synonyms.core.domain.Result.*

class ThesaurusSuite extends munit.FunSuite:

  equalityFixture.test("Synonym to Word equality") {
    _.foreach { case (string1, string2) =>
      assert(clue(Synonym(string1)) === clue(Word.applyUnsafe(string2)))
      assert(clue(Synonym(string2)) === clue(Word.applyUnsafe(string1)))
    }
  }

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

  def equalityFixture = FunFixture[List[(String, String)]](
    _ =>
      List(
        "foo"         -> "foo",
        "foo"         -> "FOO",
        "foo-bar baz" -> "foo bar-baz",
        "foo's"       -> "foos"
      ),
    _ => ()
  )
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
