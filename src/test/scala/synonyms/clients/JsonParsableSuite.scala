package synonyms.clients

import _root_.io.github.iltotore.iron.*
import cats.effect.IO
import fs2.*
import fs2.data.json.*
import fs2.data.json.circe.*
import fs2.data.json.codec.*
import fs2.io.file.{Files, Path}
import synonyms.clients.BaseThesaurusSuite.*
import synonyms.domain.Thesaurus.Datamuse
import synonyms.domain.{PartOfSpeech, ThesaurusName, Word}

class JsonParsableSuite extends BaseThesaurusSuite:
  def parseResource(name: String): IO[List[Datamuse.Word]] =
    val url = getClass.getResource(name)
    Files[IO]
      .readAll(Path(url.getPath))
      .through(text.utf8.decode)
      .through(tokens)
      .through(deserialize[IO, List[Datamuse.Word]])
      .compile
      .toList
      .map(_.flatten)

  testBuildEntriesIO(
    "parseDocument for Datamuse scrapes page successfully",
    parseResource("/dm-far.json").flatMap(words =>
      implicitly[JsonParsable[IO, Datamuse]].parseDocument(Word("far"), words)
    ),
    List(
      ExpectedEntry(
        ThesaurusName("Datamuse"),
        Word("far"),
        PartOfSpeech.Adjective,
        None,
        None,
        12
      ),
      ExpectedEntry(
        ThesaurusName("Datamuse"),
        Word("far"),
        PartOfSpeech.Noun,
        None,
        None,
        5
      ),
      ExpectedEntry(
        ThesaurusName("Datamuse"),
        Word("far"),
        PartOfSpeech.Verb,
        None,
        None,
        1
      ),
      ExpectedEntry(
        ThesaurusName("Datamuse"),
        Word("far"),
        PartOfSpeech.Adverb,
        None,
        None,
        87
      )
    )
  )
