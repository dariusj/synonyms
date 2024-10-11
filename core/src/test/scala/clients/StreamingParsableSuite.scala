package synonyms.core.clients

import _root_.io.github.iltotore.iron.*
import cats.effect.IO
import fs2.*
import fs2.io.file.{Files, Path}
import synonyms.core.clients.BaseThesaurusSuite.*
import synonyms.core.domain.Thesaurus.Datamuse
import synonyms.core.domain.{PartOfSpeech, ThesaurusName, Word}

class StreamingParsableSuite extends BaseThesaurusSuite:
  def parseResource(name: String): Stream[IO, Byte] =
    val url = getClass.getResource(name)
    Files[IO].readAll(Path(url.getPath))

  testBuildEntriesIO(
    "parseDocument for Datamuse scrapes page successfully",
    summon[StreamingParsable[IO, Datamuse]]
      .parseDocument(Word("far"), parseResource("/dm-far.json"))
      .compile
      .toList,
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
