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
      summon[JsonParsable[IO, Datamuse]].parseDocument(Word("far"), words)
    ),
    ExpectedResult(
      ThesaurusName("Datamuse"),
      Word("far"),
      List(
        ExpectedResult.Entry(PartOfSpeech.Adjective, None, None, 12),
        ExpectedResult.Entry(PartOfSpeech.Noun, None, None, 5),
        ExpectedResult.Entry(PartOfSpeech.Verb, None, None, 1),
        ExpectedResult.Entry(PartOfSpeech.Adverb, None, None, 87)
      )
    )
  )
