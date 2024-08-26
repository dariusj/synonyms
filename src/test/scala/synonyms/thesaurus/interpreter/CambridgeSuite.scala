package synonyms.thesaurus.interpreter

import cats.effect.IO
import synonyms.thesaurus.*
import synonyms.thesaurus.interpreter.BaseThesaurusSuite.*

class CambridgeSuite extends BaseThesaurusSuite:
  val cambridge = new Cambridge[IO]
  testBuildEntriesIO(
    "Cambridge.buildEntries scrapes page successfully",
    cambridge
      .buildEntries(
        Word("far"),
        BaseThesaurusSuite.parseFile(cambridge.browser, "/cam-far.html")
      ),
    List(
      ExpectedEntry(
        ThesaurusName("Cambridge"),
        Word("far"),
        PartOfSpeech.Adverb,
        None,
        Some(Example("Our land extends far beyond the fence.")),
        9
      ),
      ExpectedEntry(
        ThesaurusName("Cambridge"),
        Word("far"),
        PartOfSpeech.Adverb,
        None,
        Some(Example("The weather was far worse than we expected.")),
        9
      ),
      ExpectedEntry(
        ThesaurusName("Cambridge"),
        Word("far"),
        PartOfSpeech.Adjective,
        None,
        Some(Example("I long to travel to far places.")),
        8
      )
    )
  )
