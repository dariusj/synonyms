package synonyms.thesaurus.interpreter

import _root_.io.circe.*
import _root_.io.circe.generic.semiauto.*
import cats.effect.IO
import fs2.*
import fs2.data.json.*
import fs2.data.json.circe.*
import fs2.data.json.codec.*
import fs2.io.file.{Files, Path}
import synonyms.thesaurus.ThesaurusName
import synonyms.thesaurus.interpreter.BaseThesaurusSuite.*
import synonyms.thesaurus.interpreter.Datamuse.DatamuseWord

class DatamuseSuite extends BaseThesaurusSuite:

  def parseResource(name: String): IO[List[DatamuseWord]] =
    val url = getClass.getResource(name)
    Files[IO]
      .readAll(Path(url.getPath))
      .through(text.utf8.decode)
      .through(tokens)
      .through(deserialize[IO, List[DatamuseWord]])
      .compile
      .toList
      .map(_.flatten)

  testBuildEntriesIO(
    "Datamuse.buildEntries scrapes page successfully",
    parseResource("/dm-far.json").map(words =>
      Datamuse.buildEntries("far", words)
    ),
    List(
      ExpectedEntry(
        ThesaurusName("Datamuse"),
        "far",
        "adj",
        None,
        None,
        12
      ),
      ExpectedEntry(
        ThesaurusName("Datamuse"),
        "far",
        "n",
        None,
        None,
        5
      ),
      ExpectedEntry(
        ThesaurusName("Datamuse"),
        "far",
        "v",
        None,
        None,
        1
      ),
      ExpectedEntry(
        ThesaurusName("Datamuse"),
        "far",
        "adv",
        None,
        None,
        87
      )
    )
  )
