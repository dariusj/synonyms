package synonyms.services

import cats.effect.IO
import io.github.iltotore.iron.*
import synonyms.PropHelpers.*
import synonyms.clients.ThesaurusClient
import synonyms.domain.*
import synonyms.domain.Result.*
import synonyms.domain.Thesaurus.*
import synonyms.resources.ThesaurusClients

class SynonymsSuite extends munit.CatsEffectSuite:
  entryStore.test(
    "Synonyms.checkSynonyms matches if second word is synonym of first word"
  ) { case (entries, thesaurus, clients) =>
    Synonyms.make(clients).checkSynonyms(Word("foo"), Word("bar"), thesaurus).map {
      case _: AreSynonyms => true
      case _: NotSynonyms => fail("Failed to find synonym", clues(entries))
    }
  }

  entryStore.test(
    "Synonyms.checkSynonyms matches if first word is synonym of second word"
  ) { case (entries, thesaurus, clients) =>
    Synonyms.make(clients).checkSynonyms(Word("bar"), Word("foo"), thesaurus).map {
      case _: AreSynonyms => true
      case _: NotSynonyms => fail("Failed to find synonym", clues(entries))
    }
  }

  entryStore.test(
    "Synonyms.checkSynonyms doesn't match if words aren't synonyms"
  ) { case (entries, thesaurus, clients) =>
    Synonyms.make(clients).checkSynonyms(Word("foo"), Word("baz"), thesaurus).map {
      case synonym: AreSynonyms => fail("Found synonym", clues(entries, synonym))
      case _: NotSynonyms       => true
    }
  }

  entryStore.test(
    "Synonyms.checkSynonyms2 matches if one of the clients finds the synonym"
  ) { case (entries, _, _) =>
    val client1 = testThesaurusClient(Map(Word("foo") -> entries(Word("foo"))))
    val client2 = testThesaurusClient(entries)
    val clients = testThesaurusClients(Map(Cambridge -> client1, MerriamWebster -> client2))
    Synonyms
      .make(clients)
      .checkSynonyms2(Word("foo"), Word("bar"), clients.clients.keys.toList)
      .map {
        case result: AreSynonyms =>
          assertEquals(result.source, entries(Word("foo")).head.thesaurusName)
        case _: NotSynonyms => fail("Failed to find synonym")
      }
  }

  def entryStore =
    FunFixture[(Map[Word, List[Entry]], Thesaurus, ThesaurusClients[IO])](
      _ =>
        (for
          foo       <- entryGen.map(_.copy(synonyms = List(Synonym("bar"))))
          bar       <- entryGen.map(_.copy(synonyms = Nil))
          baz       <- entryGen.map(_.copy(synonyms = Nil))
          thesaurus <- thesaurusGen
          entries = Map("foo" -> foo, "bar" -> bar, "baz" -> baz).map { (k, v) =>
            Word.assume(k) -> List(v)
          }
        yield (
          entries,
          thesaurus,
          testThesaurusClients(Map(thesaurus -> testThesaurusClient(entries)))
        )).sample.get,
      _ => ()
    )

  private def testThesaurusClients(
      thesauruses: Map[Thesaurus, ThesaurusClient[IO]]
  ) =
    new ThesaurusClients[IO] {
      override def clients: Map[Thesaurus, ThesaurusClient[IO]] = thesauruses
    }

  private def testThesaurusClient(entries: Map[Word, List[Entry]]) =
    new ThesaurusClient[IO] {
      type Doc = List[Entry]

      override def fetchDocument(word: Word): IO[Option[Doc]] =
        IO.pure(entries.get(word))

      override def parseDocument(word: Word, document: Doc): IO[List[Entry]] =
        IO.pure(document)
    }
