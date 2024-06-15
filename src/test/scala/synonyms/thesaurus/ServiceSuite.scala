package synonyms.thesaurus

import cats.effect.IO
import synonyms.thesaurus.PropHelpers.*
import synonyms.thesaurus.Result.*
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.algebra.Client.FetchError

class ServiceSuite extends munit.CatsEffectSuite:
  entryStore.test(
    "Client.checkSynonyms matches if second word is synonym of first word"
  ) { entries =>
    val client = testClient(entries)
    Service()
      .checkSynonyms("foo", "bar", client)
      .map {
        case _: AreSynonyms => true
        case _: NotSynonyms => fail("Failed to find synonym", clues(entries))
      }
      .value
  }

  entryStore.test(
    "Client.checkSynonyms matches if first word is synonym of second word"
  ) { entries =>
    val client = testClient(entries)
    Service()
      .checkSynonyms("bar", "foo", client)
      .map {
        case _: AreSynonyms => true
        case _: NotSynonyms => fail("Failed to find synonym", clues(entries))
      }
      .value
  }

  entryStore.test(
    "Client.checkSynonyms doesn't match if words aren't synonyms"
  ) { entries =>
    val client = testClient(entries)
    Service()
      .checkSynonyms("foo", "baz", client)
      .map {
        case synonym: AreSynonyms =>
          fail("Found synonym", clues(entries, synonym))
        case _: NotSynonyms => true
      }
      .value
  }

  entryStore.test(
    "Client.checkSynonyms2 matches if one of the clients finds the synonym"
  ) { entries =>
    val client1 = testClient(Map("foo" -> entries("foo")))
    val client2 = testClient(entries)
    val clients = List(client1, client2)
    Service()
      .checkSynonyms2("foo", "bar", clients)
      .map {
        case result: AreSynonyms => assertEquals(result.source, client1.name)
        case _: NotSynonyms      => fail("Failed to find synonym")
      }
      .value
  }

  def entryStore = FunFixture[Map[String, List[Entry]]](
    _ =>
      (for
        foo <- entryGen.map(_.copy(synonyms = List("bar")))
        bar <- entryGen.map(_.copy(synonyms = Nil))
        baz <- entryGen.map(_.copy(synonyms = Nil))
      yield Map(
        "foo" -> List(foo),
        "bar" -> List(bar),
        "baz" -> List(baz)
      )).sample.get,
    _ => ()
  )

  def testClient(entries: Map[String, List[Entry]]) = new Client[IO] {
    type Doc = List[Entry]

    override val name: ThesaurusName = thesaurusNameGen.sample.get

    override def fetchDocument(
        word: String
    ): IO[Either[FetchError, Option[Doc]]] = IO.pure(Right(entries.get(word)))

    override def buildEntries(word: String, document: Doc): List[Entry] =
      document.map(_.copy(thesaurusName = name))
  }
