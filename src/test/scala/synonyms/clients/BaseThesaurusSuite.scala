package synonyms.clients

import cats.effect.IO
import munit.*
import net.ruippeixotog.scalascraper.browser.Browser
import synonyms.domain.Definition
import synonyms.domain.Entry
import synonyms.domain.Example
import synonyms.domain.PartOfSpeech
import synonyms.domain.ThesaurusName
import synonyms.domain.Word

import BaseThesaurusSuite.*

abstract class BaseThesaurusSuite extends CatsEffectSuite:
  def testBuildEntriesIO(
      name: String,
      gotEntriesIO: IO[List[Entry]],
      expectedEntries: List[ExpectedEntry]
  )(using Location): Unit =
    test(name) {
      gotEntriesIO.map { gotEntries =>
        assertEquals(gotEntries.size, expectedEntries.size)
        gotEntries.zip(expectedEntries).foreach(assertEntry.tupled)
      }
    }

  def assertEntry(got: Entry, expected: ExpectedEntry)(using Location): Unit =
    import expected.*
    import got.*
    assertEquals(thesaurusName, expectedThesaurusName)
    assertEquals(word, expectedWord)
    assertEquals(partOfSpeech, expectedPos)
    assertEquals(definition, expectedDefinition)
    assertEquals(example, expectedExample)
    assertEquals(synonyms.size, expectedSynonymCount)

object BaseThesaurusSuite:
  case class ExpectedEntry(
      expectedThesaurusName: ThesaurusName,
      expectedWord: Word,
      expectedPos: PartOfSpeech,
      expectedDefinition: Option[Definition],
      expectedExample: Option[Example],
      expectedSynonymCount: Int
  )

  def parseFile(browser: Browser, resource: String): browser.DocumentType =
    val url = getClass.getResource(resource)
    browser.parseFile(url.getPath)
