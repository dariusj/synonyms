package synonyms.thesaurus

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.*
import org.scalacheck.Prop.*
import synonyms.thesaurus.PropHelpers.given
import synonyms.thesaurus.response.Result.*
import synonyms.thesaurus.response.SynonymsByLength

class PackageSuite extends munit.ScalaCheckSuite:

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

  property("SynonymsByLength.fromEntries creates objects correctly") {
    forAllNoShrink { (entries: List[Entry]) =>
      val result = SynonymsByLength.fromEntries(entries)
      assertEquals(result.map(_._1), result.map(_._1).sorted)
      result.foreach { case SynonymsByLength(length, synonyms) =>
        synonyms.foreach(synonym =>
          assertEquals(clue(synonym.toString).length, length)
        )
        assertEquals(synonyms, synonyms.distinct.sorted)
      }
    }
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
