//> using dep com.lihaoyi::upickle:3.3.1
//> using dep net.ruippeixotog::scala-scraper:3.1.1
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.given
import scala.util.Failure
import scala.util.Success

// final case class Meaning(word: Vector[String])
// final case class Entry(word: String, synonyms: Vector[String])
// final case class Thesaurus(entries: Vector[Entry])

final case class EntryItem(definition: String, synonyms: Vector[String])
final case class Entry(
    word: String,
    partOfSpeech: String,
    entryItems: Vector[EntryItem]
)

val (first, second) = args.toList match
  case f :: s :: Nil => f -> s
  case args =>
    System.err.println(s"incorrect number of arguments: $args")
    sys.exit(1)

def checkSynonyms(first: String, second: String): Future[Boolean] =
  val firstF = Future(Scraper.synonyms(first))
  val secondF = Future(Scraper.synonyms(second))
  for {
    firstSynonyms <- firstF
    _ = println(s"Synonyms of $first: ${firstSynonyms.mkString(", ")}")
    secondSynonyms <- secondF
    _ = println(s"Synonyms of $second: ${secondSynonyms.mkString(", ")}")
  } yield firstSynonyms.contains(second) || secondSynonyms.contains(first)

val areSynonyms = checkSynonyms(first, second).map { isSynonym =>
  if (isSynonym)
    println(s"$first and $second are synonyms")
  else
    println(s"$first and $second are not synonyms")
}

Await.result(areSynonyms, 5.seconds)

object Scraper:
  import net.ruippeixotog.scalascraper.browser.JsoupBrowser
  import net.ruippeixotog.scalascraper.model.Document

  import net.ruippeixotog.scalascraper.dsl.DSL._
  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
  import net.ruippeixotog.scalascraper.dsl.DSL.Parse._

  import net.ruippeixotog.scalascraper.model._

  val browser = JsoupBrowser()
  def synonyms(word: String): List[String] =
    // val doc: Document = browser.get(
    //   "https://www.merriam-webster.com/thesaurus/$word"
    // )
    val doc = browser.parseFile("far.html")
    val entries = doc >> elementList(".thesaurus-entry-container")
    val elements =
      browser.parseFile("far.html") >> elementList(
        ".synonyms_list .thes-word-list-item"
      )

    elements.map(_.text)
