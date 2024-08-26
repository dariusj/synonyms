package synonyms.thesaurus.interpreter

import cats.effect.IO
import synonyms.thesaurus.*
import synonyms.thesaurus.interpreter.BaseThesaurusSuite.*

class MerriamWebsterSuite extends BaseThesaurusSuite:
  val mw = MerriamWebster[IO]
  testBuildEntriesIO(
    "MerriamWebster.buildEntries scrapes page successfully",
    mw.buildEntries(
      Word("far"),
      parseFile(mw.browser, "/mw-far.html")
    ),
    List(
      ExpectedEntry(
        ThesaurusName("Merriam-Webster"),
        Word("far"),
        PartOfSpeech.Adverb,
        Some(Definition("to a great degree")),
        Some(
          Example(
            "the solid advice that if you can't say something good about a person, it is far better to say nothing at all"
          )
        ),
        125
      ),
      ExpectedEntry(
        ThesaurusName("Merriam-Webster"),
        Word("far"),
        PartOfSpeech.Adjective,
        Some(Definition("lasting for a considerable time")),
        Some(
          Example(
            "the primitive rafts that ancient peoples built for their far journeys across the wide expanses of Oceania"
          )
        ),
        24
      ),
      ExpectedEntry(
        ThesaurusName("Merriam-Webster"),
        Word("far"),
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
