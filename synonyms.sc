//> using dep com.lihaoyi::upickle:3.3.1
//> using dep net.ruippeixotog::scala-scraper:3.1.1

// final case class Meaning(word: Vector[String])
// final case class Entry(word: String, synonyms: Vector[String])
// final case class Thesaurus(entries: Vector[Entry])

val (first, second) = args.toList match
  case f :: s :: Nil => f -> s
  case args =>
    System.err.println(s"incorrect number of arguments: $args")
    sys.exit(1)

// val t = Thesaurus(Vector(Entry("brave", Vector("courageous"))))
// def areSynonyms(first: String, second: String): Boolean =
//   t.entries.find(_.word == first).exists(_.synonyms.contains(second))

val allSynonyms = Scraper.synonyms(first)
println(s"Synonyms: ${allSynonyms.mkString(", ")}")

if (allSynonyms.contains(second))
  println(s"$first and $second are synonyms")
else
  println(s"$first and $second are not synonyms")

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
