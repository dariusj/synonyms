package synonyms.core.programs

import cats.effect.IO
import io.github.iltotore.iron.*
import munit.*
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import synonyms.core.PropHelpers.*
import synonyms.core.config.types.SynonymConfig
import synonyms.core.domain.*
import synonyms.core.domain.Result.*
import synonyms.core.domain.Thesaurus.*
import synonyms.core.interpreters.*
import synonyms.core.services.ThesaurusService

class SynonymsSuite extends CatsEffectSuite with ScalaCheckEffectSuite:
  entryStore.test(
    "Synonyms.checkSynonyms matches if second word is synonym of first word"
  ) { case (entries, thesaurus, service, config) =>
    Synonyms(service, config).checkSynonyms(Word("foo"), Word("bar"), List(thesaurus)).map {
      case _: AreSynonyms => true
      case _: NotSynonyms => fail("Failed to find synonym", clues(entries))
    }
  }

  entryStore.test(
    "Synonyms.checkSynonyms matches if first word is synonym of second word"
  ) { case (entries, thesaurus, service, config) =>
    Synonyms(service, config).checkSynonyms(Word("bar"), Word("foo"), List(thesaurus)).map {
      case _: AreSynonyms => true
      case _: NotSynonyms => fail("Failed to find synonym", clues(entries))
    }
  }

  entryStore.test(
    "Synonyms.checkSynonyms doesn't match if words aren't synonyms"
  ) { case (entries, thesaurus, service, config) =>
    Synonyms(service, config).checkSynonyms(Word("foo"), Word("baz"), List(thesaurus)).map {
      case synonym: AreSynonyms => fail("Found synonym", clues(entries, synonym))
      case _: NotSynonyms       => true
    }
  }

  entryStore.test(
    "Synonyms.checkSynonyms matches if one of the clients finds the synonym"
  ) { case (entries, _, _, config) =>
    val client1 = TestThesaurusClient(Map(Word("foo") -> entries(Word("foo"))))
    val client2 = TestThesaurusClient(entries)
    val clients = TestThesaurusClients(Map(Cambridge -> client1, MerriamWebster -> client2))
    Synonyms(ThesaurusService.make(clients), config)
      .checkSynonyms(Word("foo"), Word("bar"), clients.clients.keys.toList)
      .map {
        case result: AreSynonyms =>
          assertEquals(result.source, entries(Word("foo")).head.thesaurusName)
        case _: NotSynonyms => fail("Failed to find synonym")
      }
  }

  test("Synonyms.synonymsByLength returns unique synonyms from all thesauruses") {
    PropF.forAllF {
      for
        word     <- wordGen
        (t1, t2) <- Gen.zip(thesaurusGen, thesaurusGen).suchThat((t1, t2) => t1 != t2)
        entries1 <- Gen.nonEmptyListOf(
          entryGen.map(_.copy(word = word, thesaurusName = t1.name))
        )
        entries2 <- Gen.nonEmptyListOf(
          entryGen.map(_.copy(word = word, thesaurusName = t2.name))
        )
        characterSet <- characterSetGen
      yield
        val client1 = TestThesaurusClient(Map(word -> entries1))
        val client2 = TestThesaurusClient(Map(word -> entries2))
        val clients = TestThesaurusClients(Map(t1 -> client1, t2 -> client2))
        val service = ThesaurusService.make(clients)
        (
          word,
          service,
          List(t1, t2),
          entries1 ++ entries2,
          SynonymConfig(characterSet, DefaultStringSize)
        )
    } { case (word, service, thesauruses, entries, config) =>
      Synonyms(service, config).synonymsByLength(word, thesauruses).map { syns =>
        val gotSynonyms      = syns.flatMap(_.synonyms)
        val expectedSynonyms = entries.flatMap(_.synonyms)
        assertEquals(gotSynonyms.sorted, expectedSynonyms.distinct.sorted)
      }
    }
  }

  def entryStore =
    FunFixture[(Map[Word, List[Entry]], Thesaurus, ThesaurusService[IO], SynonymConfig)](
      _ =>
        val fixtureGen = for
          thesaurus <- thesaurusGen
          foo <- entryGen.map(
            _.copy(synonyms = List(Synonym("bar")), thesaurusName = thesaurus.name)
          )
          bar          <- entryGen.map(_.copy(synonyms = Nil, thesaurusName = thesaurus.name))
          baz          <- entryGen.map(_.copy(synonyms = Nil, thesaurusName = thesaurus.name))
          characterSet <- characterSetGen
          entries = Map("foo" -> foo, "bar" -> bar, "baz" -> baz).map { (k, v) =>
            Word.assume(k) -> List(v)
          }
          clients = TestThesaurusClients(Map(thesaurus -> TestThesaurusClient(entries)))
        yield (entries, thesaurus, ThesaurusService.make(clients), SynonymConfig(characterSet, 15))
        fixtureGen.sample.get
      ,
      _ => ()
    )
