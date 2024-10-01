package synonyms.clients

import cats.effect.IO
import io.github.iltotore.iron.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import synonyms.clients.BaseThesaurusSuite.*
import synonyms.domain.*
import synonyms.domain.Thesaurus.*

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

  testBuildEntriesIO(
    "parseDocument for WordHippo parses page successfully",
    summon[JsoupParsable[IO, WordHippo]].parseDocument(
      Word("far"),
      BaseThesaurusSuite.parseFile(JsoupBrowser(), "/wh-far.html")
    ),
    ExpectedResult(
      ThesaurusName("WordHippo"),
      Word("far"),
      List(
        ExpectedResult.Entry(
          PartOfSpeech.Adjective,
          Some(Definition("Situated at a great distance in space or time")),
          Some(
            Example(
              "In the far distance, along the humping road, an army truck crawls up the horizon towards us."
            )
          ),
          58
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adjective,
          Some(
            Definition("Being far, or the furthest distance, or the opposite end, from a point")
          ),
          Some(
            Example(
              "I went to the position of Captain Weir's company at the far point of the ridge down-stream."
            )
          ),
          17
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adjective,
          Some(Definition("Remote or isolated, possibly inaccessible")),
          Some(
            Example(
              "Then she had met Durand Laxart, a young widower who had recently come to Burey from some far village."
            )
          ),
          124
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adjective,
          Some(Definition("Lasting or extending for a great period of time")),
          Some(
            Example(
              "In his mind's eye, he followed the survivors of the persecution on their far journeys to the world's end."
            )
          ),
          166
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adjective,
          Some(Definition("Furthest from a center")),
          None,
          102
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adjective,
          Some(Definition("At a distance from an intended target")),
          None,
          278
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adjective,
          Some(Definition("Small or improbable in degree")),
          None,
          59
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adverb,
          Some(Definition("By a great deal")),
          Some(
            Example(
              "While an improvement on the previous plan, it still falls far short of acceptable."
            )
          ),
          338
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adverb,
          Some(Definition("To or from a great distance, time, or degree")),
          Some(
            Example(
              "The mountains looming far to my right, the West Alps, told me we had crossed into France."
            )
          ),
          22
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adverb,
          Some(Definition("To, or at a distance from, a particular place, person, or thing")),
          None,
          52
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adverb,
          Some(Definition("Everywhere, widely in different directions")),
          None,
          85
        ),
        ExpectedResult.Entry(
          PartOfSpeech.Adverb,
          Some(Definition("To a late or an advanced time")),
          None,
          4
        )
      )
    )
  )

  testBuildEntriesIO(
    "parseDocument for PowerThesaurus parses page successfully",
    summon[JsoupParsable[IO, PowerThesaurus]].parseDocument(
      Word("far"),
      BaseThesaurusSuite.parseFile(JsoupBrowser(), "/pt-far.html")
    ),
    ExpectedResult(
      ThesaurusName("PowerThesaurus"),
      Word("far"),
      List(
        ExpectedResult.Entry(PartOfSpeech.Adjective, None, None, 29),
        ExpectedResult.Entry(PartOfSpeech.Adverb, None, None, 36),
        ExpectedResult.Entry(PartOfSpeech.Noun, None, None, 1),
        ExpectedResult.Entry(PartOfSpeech.Undetermined, None, None, 2),
        ExpectedResult.Entry(PartOfSpeech.Verb, None, None, 3)
      )
    )
  )
