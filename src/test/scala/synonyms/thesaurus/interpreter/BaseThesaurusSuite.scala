package synonyms.thesaurus.interpreter

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import synonyms.thesaurus.Entry
import synonyms.thesaurus.ThesaurusName

import BaseThesaurusSuite.*

abstract class BaseThesaurusSuite extends munit.FunSuite:
  def testBuildEntries(
      name: String,
      gotEntries: List[Entry],
      expectedEntries: List[ExpectedEntry]
  )(implicit loc: munit.Location): Unit =
    test(name) {
      if gotEntries.size != expectedEntries.size then
        fail("Unexpected number of entries", clues(gotEntries))
      else gotEntries.zip(expectedEntries).foreach(assertEntry.tupled)
    }

  def assertEntry(got: Entry, expected: ExpectedEntry): Unit =
    import got.*
    import expected.*
    assertEquals(thesaurusName, expectedThesaurusName)
    assertEquals(word, expectedWord)
    assertEquals(partOfSpeech, expectedPos)
    assertEquals(definition, expectedDefinition)
    assertEquals(example, expectedExample)
    assertEquals(synonyms.size, expectedSynonymCount)

object BaseThesaurusSuite:
  final case class ExpectedEntry(
      expectedThesaurusName: ThesaurusName,
      expectedWord: String,
      expectedPos: String,
      expectedDefinition: Option[String],
      expectedExample: String,
      expectedSynonymCount: Int
  )

  def parseFile(resource: String): Document =
    val url = getClass.getResource(resource)
    JsoupBrowser().parseFile(url.getPath)
