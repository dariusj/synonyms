package synonyms.clients

import cats.effect.IO
import io.github.iltotore.iron.*
import net.ruippeixotog.scalascraper.browser.HtmlUnitBrowser
import synonyms.clients.BaseThesaurusSuite.*
import synonyms.domain.*
import synonyms.domain.Thesaurus.Collins

class HtmlUnitParsableSuite extends BaseThesaurusSuite:
  testBuildEntriesIO(
    "parseDocument for Collins parses page successfully",
    summon[HtmlUnitParsable[IO, Collins]]
      .parseDocument(Word("far"), parseFile(HtmlUnitBrowser(), "/collins-far.html")),
    List(
      ExpectedEntry(
        ThesaurusName("Collins"),
        Word("far"),
        PartOfSpeech.Adverb,
        Some(Definition("at, to, or from a great distance")),
        Some(Example("They came from far away.")),
        6
      ),
      ExpectedEntry(
        ThesaurusName("Collins"),
        Word("far"),
        PartOfSpeech.Adverb,
        Some(Definition("by a considerable degree")),
        Some(Example("He was a far better cook than Amy.")),
        9
      ),
      ExpectedEntry(
        ThesaurusName("Collins"),
        Word("far"),
        PartOfSpeech.Adjective,
        Some(Definition("more distant")),
        Some(Example("He wandered to the far end of the room.")),
        6
      ),
      ExpectedEntry(
        ThesaurusName("Collins"),
        Word("far"),
        PartOfSpeech.Adjective,
        Some(Definition("distant in space or time")),
        Some(Example("people in far lands")),
        10
      ),
      ExpectedEntry(
        ThesaurusName("Collins"),
        Word("far"),
        PartOfSpeech.Adverb,
        Some(Definition("in the sense of a long way")),
        None,
        6
      ),
      ExpectedEntry(
        ThesaurusName("Collins"),
        Word("far"),
        PartOfSpeech.Adverb,
        Some(Definition("in the sense of much")),
        None,
        7
      ),
      ExpectedEntry(
        ThesaurusName("Collins"),
        Word("far"),
        PartOfSpeech.Adjective,
        Some(Definition("in the sense of remote")),
        None,
        7
      )
    )
  )
