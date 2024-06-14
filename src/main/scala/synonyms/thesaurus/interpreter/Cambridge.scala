package synonyms.thesaurus.interpreter

import cats.effect.IO
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import org.jsoup.HttpStatusException
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client.*

import scala.util.*
import cats.syntax.either.*

object Cambridge extends Scraper[IO]:
  override val name: ThesaurusName = ThesaurusName("Cambridge")

  def url(word: String) = s"https://dictionary.cambridge.org/thesaurus/$word"

  override def fetchDocument(word: String): IO[Either[ClientError, Doc]] =
    IO(browser.get(url(word))).attempt.flatMap {
      case Right(v) => IO.pure(v.asRight)
      case Left(e: HttpStatusException) if e.getStatusCode == 404 =>
        IO.pure(NotFound(word, e.getUrl).asLeft)
      case Left(e) => IO.raiseError(e)
    }

  override def buildEntries(word: String, document: Doc): List[Entry] =
    val entryEls = (document >> elementList(".entry-block:has(.pos) > div"))
    entryEls
      .foldLeft(Option.empty[(String, List[Entry])]) {
        case (acc, el) if el.attr("class").split(" ").contains("lmb-10") =>
          val pos = el >> text(".pos")
          Some(pos, acc.fold(Nil) { case (_, entries) => entries })
        case (Some(pos, entries), el)
            if el.attr("class").split(" ").contains("sense") =>
          val example  = el >> text(".eg")
          val synonyms = el >> texts(".synonym")

          Some(
            pos,
            entries :+ Entry(name, word, pos, None, example, synonyms.toList)
          )
        case (acc, _) => acc
      }
      .fold(Nil) { case (_, entries) => entries }
