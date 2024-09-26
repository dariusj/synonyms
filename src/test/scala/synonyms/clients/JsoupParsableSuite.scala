package synonyms.clients

import cats.effect.IO
import io.github.iltotore.iron.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import synonyms.clients.BaseThesaurusSuite.*
import synonyms.domain.*
import synonyms.domain.Thesaurus.{Cambridge, MerriamWebster}

class JsoupParsableSuite extends BaseThesaurusSuite:
  testBuildEntriesIO(
    "parseDocument for MerriamWebster parses page successfully",
    summon[JsoupParsable[IO, MerriamWebster]]
      .parseDocument(Word("far"), parseFile(JsoupBrowser(), "/mw-far.html")),
    ExpectedResult(
      ThesaurusName("Merriam-Webster"),
      Word("far"),
      List(
        ExpectedResult.Entry(
          PartOfSpeech.Adverb,
          Some(Definition("to a great degree")),
          Some(
            Example(
              "the solid advice that if you can't say something good about a person, it is far better to say nothing at all"
            )
          ),
          125
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adjective,
          Some(Definition("lasting for a considerable time")),
          Some(
            Example(
              "the primitive rafts that ancient peoples built for their far journeys across the wide expanses of Oceania"
            )
          ),
          24
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adjective,
          Some(Definition("not close in time or space")),
          Some(
            Example(
              "the dream of someday sending manned spacecraft to explore the far reaches of our solar system"
            )
          ),
          21
        )
      )
    )
  )

  testBuildEntriesIO(
    "parseDocument for Cambridge parses page successfully",
    summon[JsoupParsable[IO, Cambridge]].parseDocument(
      Word("far"),
      BaseThesaurusSuite.parseFile(JsoupBrowser(), "/cam-far.html")
    ),
    ExpectedResult(
      ThesaurusName("Cambridge"),
      Word("far"),
      List(
        ExpectedResult.Entry(
          PartOfSpeech.Adverb,
          None,
          Some(Example("Our land extends far beyond the fence.")),
          9
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adverb,
          None,
          Some(Example("The weather was far worse than we expected.")),
          9
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adjective,
          None,
          Some(Example("I long to travel to far places.")),
          8
        )
      )
    )
  )
