package synonyms.core.services

import cats.effect.IO
import io.github.iltotore.iron.*
import munit.*
import org.scalacheck.effect.PropF
import synonyms.core.PropHelpers.*
import synonyms.core.domain.{SynonymLength, Thesaurus}
import synonyms.core.interpreters.*

class ThesaurusServiceSuite extends CatsEffectSuite with ScalaCheckEffectSuite:
  test("ThesaurusService.getEntries") {
    PropF.forAllF(
      for
        word         <- wordGen
        entries      <- nonEmptyListGen(entryGen)
        characterSet <- characterSetGen
        thesaurus    <- thesaurusGen
      yield
        val client  = TestThesaurusClient(Map(word -> entries))
        val clients = TestThesaurusClients(Map(thesaurus -> client))
        (word, clients, thesaurus, characterSet)
    ) { case (word, clients, thesaurus, characterSet) =>
      val maxLength = SynonymLength(DefaultStringSize - 5)
      val service   = ThesaurusService.make(clients)

      for entries <- service.getEntries(word, thesaurus, maxLength, characterSet)
      yield
        entries.foreach { entry =>
          assert(clue(entry).synonyms.nonEmpty)
        }
        val synonyms = entries.flatMap(_.synonyms)
        synonyms.foreach { synonym =>
          assert(clue(synonym).countChars(characterSet) <= maxLength)
        }
    }
  }
