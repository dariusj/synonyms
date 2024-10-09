package synonyms.core.domain

import org.scalacheck.Gen
import org.scalacheck.Prop.*
import synonyms.core.PropHelpers.*
import synonyms.core.domain.*

class ResponsesSuite extends munit.ScalaCheckSuite:

  property("SynonymsByLength.fromEntries creates objects correctly") {
    forAllNoShrink(Gen.listOf(entryGen), characterSetGen) { case (entries, characterSet) =>
      val result = SynonymsByLength.fromEntries(entries, characterSet)
      assertEquals(result.map(_._1), result.map(_._1).sorted)
      result.foreach { case SynonymsByLength(length, synonyms) =>
        synonyms.foreach(synonym => assertEquals(clue(synonym.toString).length, length))
        assertEquals(synonyms, synonyms.distinct.sorted)
      }
    }
  }
