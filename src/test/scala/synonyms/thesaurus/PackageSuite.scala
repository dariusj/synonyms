package synonyms.thesaurus

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.*
import org.scalacheck.Prop.*
import synonyms.thesaurus.PropHelpers.given
import synonyms.thesaurus.Result.*

class PackageSuite extends munit.ScalaCheckSuite:

  entryFixture.test(
    "Entry.hasSynonym returns AreSynonyms when synonym is found"
  ) { entry =>
    assert(clue(clue(entry).hasSynonym("foo")).isInstanceOf[AreSynonyms])
  }

  entryFixture.test(
    "Entry.hasSynonym returns NotSynonyms when synonym is not found"
  ) { entry =>
    assert(clue(clue(entry).hasSynonym("baz")).isInstanceOf[NotSynonyms])
  }

  property("SynonymsByLength.fromEntries creates objects correctly") {
    forAllNoShrink { (entries: List[Entry]) =>
      val result = SynonymsByLength.fromEntries(entries)
      assertEquals(result.map(_._1), result.map(_._1).sorted)
      result.foreach { case SynonymsByLength(length, synonyms) =>
        synonyms.foreach(synonym => assertEquals(clue(synonym).length, length))
        assertEquals(synonyms, synonyms.distinct.sorted)
      }
    }
  }

  def entryFixture = FunFixture[Entry](
    _ =>
      Entry(
        ThesaurusName("thesaurusName"),
        "word",
        "pos",
        Some("definition"),
        Some("example"),
        List("foo", "bar")
      ),
    _ => ()
  )
