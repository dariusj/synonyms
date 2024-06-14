package synonyms.thesaurus.interpreter

import synonyms.thesaurus.ThesaurusName
import BaseThesaurusSuite.*

class CambridgeSuite extends BaseThesaurusSuite:
  testBuildEntries(
    "Cambridge.buildEntries scrapes page successfully",
    Cambridge
      .buildEntries(
        "far",
        BaseThesaurusSuite.parseFile(Cambridge.browser, "/cam-far.html")
      ),
    List(
      ExpectedEntry(
        ThesaurusName("Cambridge"),
        "far",
        "adverb",
        None,
        Some("Our land extends far beyond the fence."),
        9
      ),
      ExpectedEntry(
        ThesaurusName("Cambridge"),
        "far",
        "adverb",
        None,
        Some("The weather was far worse than we expected."),
        9
      ),
      ExpectedEntry(
        ThesaurusName("Cambridge"),
        "far",
        "adjective",
        None,
        Some("I long to travel to far places."),
        8
      )
    )
  )
