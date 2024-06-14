package synonyms.thesaurus

import cats.effect.IO
import synonyms.thesaurus.algebra.Client

import PropHelpers.*

class ServiceSuite extends munit.CatsEffectSuite:
  entryStore.test(
    "Client.checkSynonyms matches if second word is synonym of first word"
  ) { entries =>
    val client = testClient(entries)
    Service(client).checkSynonyms("foo", "bar").map {
      case _: AreSynonyms => true
      case _: NotSynonyms => fail("Failed to find synonym", clues(entries))
    }
  }

  entryStore.test(
    "Client.checkSynonyms matches if first word is synonym of second word"
  ) { entries =>
    val client = testClient(entries)
    Service(client).checkSynonyms("bar", "foo").map {
      case _: AreSynonyms => true
      case _: NotSynonyms => fail("Failed to find synonym", clues(entries))
    }
  }

  entryStore.test(
    "Client.checkSynonyms doesn't match if words aren't synonyms"
  ) { entries =>
    val client = testClient(entries)
    Service(client).checkSynonyms("foo", "baz").map {
      case synonym: AreSynonyms =>
        fail("Found synonym", clues(entries, synonym))
      case _: NotSynonyms => true
    }
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

    override def name: ThesaurusName = ThesaurusName("Test Thesaurus")

    override def fetchDocument(word: String): IO[Doc] = IO(entries(word))

    override def buildEntries(word: String, document: Doc): List[Entry] =
      document
  }
