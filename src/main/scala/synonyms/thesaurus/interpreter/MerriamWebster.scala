package synonyms.thesaurus.interpreter

import cats.effect.IO
import cats.syntax.either.*
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Element
import org.jsoup.HttpStatusException
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client.*

object MerriamWebster extends Scraper[IO]:
  override val name: ThesaurusName = ThesaurusName("Merriam-Webster")

  def url(word: String) = s"https://www.merriam-webster.com/thesaurus/$word"

  override def fetchDocument(word: String): IO[Either[ClientError, Doc]] =
    IO(browser.get(url(word))).attempt.flatMap {
      case Right(v) => IO.pure(v.asRight)
      case Left(e: HttpStatusException) if e.getStatusCode == 404 =>
        IO.pure(NotFound(word, e.getUrl).asLeft[Doc])
      case Left(e) => IO.raiseError(e)
    }

  override def buildEntries(word: String, document: Doc): List[Entry] =
    val entryEls = document >> elementList(".thesaurus-entry-container")

    def buildEntry(pos: String)(el: Element) =
      val example    = el >> text(".dt span")
      val definition = (el >> text(".dt")).dropRight(example.length + 1)
      val synonyms = el >> texts(
        ".sim-list-scored .synonyms_list li.thes-word-list-item"
      )
      Entry(name, word, pos, Some(definition), example, synonyms.toList)

    entryEls.flatMap { entry =>
      val pos = entry >> text(".parts-of-speech")
      (entry >> elementList(".vg-sseq-entry-item")).map(buildEntry(pos))
    }
