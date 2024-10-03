package synonyms.core.domain

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.*
import org.scalacheck.Prop.*
import synonyms.core.PropHelpers.given
import synonyms.core.domain.*

class ResponsesSuite extends munit.ScalaCheckSuite:

  property("SynonymsByLength.fromEntries creates objects correctly") {
    forAllNoShrink { (entries: List[Entry]) =>
      val result = SynonymsByLength.fromEntries(entries)
      assertEquals(result.map(_._1), result.map(_._1).sorted)
      result.foreach { case SynonymsByLength(length, synonyms) =>
        synonyms.foreach(synonym => assertEquals(clue(synonym.toString).length, length))
        assertEquals(synonyms, synonyms.distinct.sorted)
      }
    }
  }
