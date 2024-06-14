package synonyms.thesaurus

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.*
import org.scalacheck.Prop.*
import synonyms.thesaurus.PropHelpers.given

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

  property("Entry.synonymsByLength sorts entries accordingly") {
    forAllNoShrink { (entries: List[Entry]) =>
      val result = Entry.synonymsByLength(entries)
      assertEquals(result.map(_._1), result.map(_._1).sorted)
      result.foreach { case (_, synonyms) =>
        assertEquals(synonyms, synonyms.sorted)
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
        "example",
        List("foo", "bar")
      ),
    _ => ()
  )
