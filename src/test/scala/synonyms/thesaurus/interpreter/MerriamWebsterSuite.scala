package synonyms.thesaurus.interpreter

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document
import synonyms.thesaurus.Entry
import synonyms.thesaurus.ThesaurusName

class MerriamWebsterSuite extends munit.FunSuite:
  test("MerriamWebster.buildEntries scrapes page successfully") {
    val document  = parseFile(s"/mw-far.html")
    val entries   = MerriamWebster.buildEntries("far", document)

    entries match
      case List(first, second, third) =>
        assertEntry(
          first,
          ThesaurusName("Merriam-Webster"),
          "far",
          "adverb",
          Some("to a great degree"),
          "",
          125
        )
        assertEntry(
          second,
          ThesaurusName("Merriam-Webster"),
          "far",
          "adjective",
          Some("lasting for a considerable time"),
          "",
          24
        )
        assertEntry(
          third,
          ThesaurusName("Merriam-Webster"),
          "far",
          "adjective",
          Some("not close in time or space"),
          "",
          21
        )
      case _ => fail(s"Unexpected number of entries", clues(entries))
  }

  def assertEntry(
      entry: Entry,
      expectedThesaurusName: ThesaurusName,
      expectedWord: String,
      expectedPos: String,
      expectedDefinition: Option[String],
      expectedExample: String,
      expectedSynonymCount: Int
  ): Unit = entry match
    case Entry(thesaurusName, word, pos, definition, example, synonyms) =>
      assertEquals(thesaurusName, expectedThesaurusName)
      assertEquals(word, expectedWord)
      assertEquals(pos, expectedPos)
      assertEquals(definition, expectedDefinition)
      assertEquals(synonyms.size, expectedSynonymCount)

  def parseFile(resource: String): Document =
    val url = getClass.getResource(resource)
    JsoupBrowser().parseFile(url.getPath)
