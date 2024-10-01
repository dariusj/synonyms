package synonyms.clients

import cats.effect.IO
import munit.*
import net.ruippeixotog.scalascraper.browser.Browser
import synonyms.domain.*

import BaseThesaurusSuite.*

abstract class BaseThesaurusSuite extends CatsEffectSuite:
  def testBuildEntriesIO(
      name: String,
      gotEntriesIO: IO[List[Entry]],
      expectedResult: ExpectedResult
  )(using Location): Unit =
    test(name) {
      gotEntriesIO.map { gotEntries =>
        import expectedResult.*
        assertEquals(clue(gotEntries).size, expectedEntries.size)
        gotEntries.zip(expectedEntries).foreach { case (gotEntry, expectedEntry) =>
          import expectedEntry.*
          import gotEntry.*
          assertEquals(thesaurusName, expectedThesaurusName)
          assertEquals(word, expectedWord)
          assertEquals(partOfSpeech, expectedPos)
          assertEquals(definition, expectedDefinition)
          assertEquals(example, expectedExample)
          assertEquals(clue(synonyms).size, expectedSynonymCount)
        }
      }
    }

object BaseThesaurusSuite:
  case class ExpectedResult(
      expectedThesaurusName: ThesaurusName,
      expectedWord: Word,
      expectedEntries: List[ExpectedResult.Entry]
  )

  object ExpectedResult:
    case class Entry(
        expectedPos: PartOfSpeech,
        expectedDefinition: Option[Definition],
        expectedExample: Option[Example],
        expectedSynonymCount: Int
    )

  def parseFile(browser: Browser, resource: String): browser.DocumentType =
    val url = getClass.getResource(resource)
    browser.parseFile(url.getPath)
