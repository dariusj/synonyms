//> using dep com.lihaoyi::upickle:3.3.1
//> using dep net.ruippeixotog::scala-scraper:3.1.1

package synonyms

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.given
import scala.util.Failure
import scala.util.Success

// final case class Meaning(word: Vector[String])
// final case class Entry(word: String, synonyms: Vector[String])
// final case class Thesaurus(entries: Vector[Entry])

@main
def synonyms(args: String*) = {
  val (first, second) = args.toList match
    case f :: s :: Nil => f -> s
    case args =>
      System.err.println(s"incorrect number of arguments: $args")
      sys.exit(1)

  val thesaurus = model.MerriamWebster

  def checkSynonyms(first: String, second: String): Future[Boolean] =
    val firstF = Future(thesaurus.synonyms(first))
    val secondF = Future(thesaurus.synonyms(second))
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
}
