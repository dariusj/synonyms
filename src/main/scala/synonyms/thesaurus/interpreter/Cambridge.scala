package synonyms.thesaurus.interpreter

import cats.effect.IO
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Document
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Thesaurus

object Cambridge extends Thesaurus[IO]:
  def url(word: String) = s"https://dictionary.cambridge.org/thesaurus/$word"

  override val name: ThesaurusName = ThesaurusName("Cambridge")

  override def fetchDocument(word: String): IO[Document] = IO(
    // browser.get(url(word))
    browser.parseFile("run-cambridge.html")
  )

  override def buildEntries(word: String, document: Document): List[Entry] =
    val entryEls = (document >> elementList(".entry-block:has(.pos) > div"))
    entryEls
      .foldLeft(Option.empty[(String, List[Entry])]) {
        case (acc, el) if el.attr("class").split(" ").contains("lmb-10") =>
          val pos = el >> text(".pos")
          Some(pos, acc.fold(Nil) { case (_, entries) => entries })
        case (Some(pos, entries), el)
            if el.attr("class").split(" ").contains("sense") =>
          val example = el >> text(".eg")
          val synonyms = el >> texts(".synonym")

          Some(
            pos,
            entries :+ Entry(name, word, pos, None, example, synonyms.toList)
          )
        case (acc, _) => acc
      }
      .fold(Nil) { case (_, entries) => entries }
