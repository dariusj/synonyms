package synonyms.core.programs

import cats.effect.IO
import io.github.iltotore.iron.*
import munit.*
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import synonyms.core.PropHelpers.*
import synonyms.core.clients.ThesaurusClient
import synonyms.core.domain.*
import synonyms.core.domain.Result.*
import synonyms.core.domain.Thesaurus.*
import synonyms.core.resources.ThesaurusClients
import synonyms.core.services.ThesaurusService

class SynonymsSuite extends CatsEffectSuite with ScalaCheckEffectSuite:
  entryStore.test(
    "Synonyms.checkSynonyms matches if second word is synonym of first word"
  ) { case (entries, thesaurus, service) =>
    Synonyms(service).checkSynonyms(Word("foo"), Word("bar"), List(thesaurus)).map {
      case _: AreSynonyms => true
      case _: NotSynonyms => fail("Failed to find synonym", clues(entries))
    }
  }

  entryStore.test(
    "Synonyms.checkSynonyms matches if first word is synonym of second word"
  ) { case (entries, thesaurus, service) =>
    Synonyms(service).checkSynonyms(Word("bar"), Word("foo"), List(thesaurus)).map {
      case _: AreSynonyms => true
      case _: NotSynonyms => fail("Failed to find synonym", clues(entries))
    }
  }

  entryStore.test(
    "Synonyms.checkSynonyms doesn't match if words aren't synonyms"
  ) { case (entries, thesaurus, service) =>
    Synonyms(service).checkSynonyms(Word("foo"), Word("baz"), List(thesaurus)).map {
      case synonym: AreSynonyms => fail("Found synonym", clues(entries, synonym))
      case _: NotSynonyms       => true
    }
  }

  entryStore.test(
    "Synonyms.checkSynonyms matches if one of the clients finds the synonym"
  ) { case (entries, _, _) =>
    val client1 = testThesaurusClient(Map(Word("foo") -> entries(Word("foo"))))
    val client2 = testThesaurusClient(entries)
    val clients = testThesaurusClients(Map(Cambridge -> client1, MerriamWebster -> client2))
    Synonyms(ThesaurusService.make(clients))
      .checkSynonyms(Word("foo"), Word("bar"), clients.clients.keys.toList)
      .map {
        case result: AreSynonyms =>
          assertEquals(result.source, entries(Word("foo")).head.thesaurusName)
        case _: NotSynonyms => fail("Failed to find synonym")
      }
  }

  test("Synonyms.synonymsByLength returns unique synonyms from all thesauruses") {
    PropF.forAllNoShrinkF {
      for
        word     <- wordGen
        (t1, t2) <- Gen.zip(thesaurusGen, thesaurusGen).suchThat((t1, t2) => t1 != t2)
        entries1 <- Gen.nonEmptyListOf(
          entryGen.map(_.copy(word = word, thesaurusName = t1.name))
        )
        entries2 <- Gen.nonEmptyListOf(
          entryGen.map(_.copy(word = word, thesaurusName = t2.name))
        )
      yield
        val client1 = testThesaurusClient(Map(word -> entries1))
        val client2 = testThesaurusClient(Map(word -> entries2))
        val clients = testThesaurusClients(Map(t1 -> client1, t2 -> client2))
        val service = ThesaurusService.make(clients)
        (word, service, List(t1, t2), entries1 ++ entries2)
    } { case (word, service, thesauruses, entries) =>
      Synonyms(service).synonymsByLength(word, thesauruses).map { syns =>
        val gotSynonyms      = syns.flatMap(_.synonyms)
        val expectedSynonyms = entries.flatMap(_.synonyms)
        assertEquals(gotSynonyms.sorted, expectedSynonyms.distinct.sorted)
      }
    }
  }

  def entryStore =
    FunFixture[(Map[Word, List[Entry]], Thesaurus, ThesaurusService[IO])](
      _ =>
        val fixtureGen = for
          thesaurus <- thesaurusGen
          foo <- entryGen.map(
            _.copy(synonyms = List(Synonym("bar")), thesaurusName = thesaurus.name)
          )
          bar <- entryGen.map(_.copy(synonyms = Nil, thesaurusName = thesaurus.name))
          baz <- entryGen.map(_.copy(synonyms = Nil, thesaurusName = thesaurus.name))
          entries = Map("foo" -> foo, "bar" -> bar, "baz" -> baz).map { (k, v) =>
            Word.assume(k) -> List(v)
          }
          clients = testThesaurusClients(Map(thesaurus -> testThesaurusClient(entries)))
        yield (entries, thesaurus, ThesaurusService.make(clients))
        fixtureGen.sample.get
      ,
      _ => ()
    )

  private def testThesaurusClients(thesauruses: Map[Thesaurus, ThesaurusClient[IO]]) =
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
