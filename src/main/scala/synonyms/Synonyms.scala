//> using option -Wunused:all

//> using dep com.monovore::decline-effect::2.4.1
//> using dep dev.optics::monocle-core::3.2.0
//> using dep dev.optics::monocle-macro::3.2.0
//> using dep net.ruippeixotog::scala-scraper:3.1.1
//> using dep org.typelevel::cats-core::2.10.0
//> using dep org.typelevel::cats-effect:3.5.4

package synonyms

import cats.data.NonEmptyList
import cats.effect.*
import cats.syntax.apply.*
import cats.syntax.show.*
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import synonyms.thesaurus.*
import synonyms.thesaurus.Service
import synonyms.thesaurus.algebra.Thesaurus
import synonyms.thesaurus.interpreter.*

object Synonyms
    extends CommandIOApp(
      "synonyms",
      "An application to check synonyms",
      true,
      "v0.1"
    ):
  def main: Opts[IO[ExitCode]] = (checkSynonyms orElse listSynonyms).map {
    case CheckSynonyms(first, second, source) =>
      given Thesaurus[IO] = source.source
      Service
        .checkSynonyms[IO](first, second)
        .flatMap(result => IO.println(result.show))
        .as(ExitCode.Success)
    case ListSynonyms(word, source) =>
      given Thesaurus[IO] = source.source
      Service
        .getEntries[IO](word)
        .flatMap(result =>
          IO.println(
            Entry
              .synonyms(result)
              .map { case (l, words) => s"($l) ${words.mkString(", ")}" }
              .mkString("\n")
          )
        )
        .as(ExitCode.Success)
  }

final case class CheckSynonyms[F[_]](
    first: String,
    second: String,
    source: Source[F]
)
final case class Source[F[_]](source: Thesaurus[F])
final case class Words(first: String, second: String)

final case class ListSynonyms[F[_]](word: String, source: Source[F])

val words: Opts[(String, String)] = Opts.arguments[String]("words").map {
  case NonEmptyList(f, s :: Nil) => f -> s
  case args =>
    throw IllegalArgumentException(s"Incorrect number of arguments: $args")
}

val source: Opts[Source[IO]] =
  Opts
    .option[String]("source", "The thesaurus to use", "s")
    .map {
      case "cambridge" => Source(Cambridge)
      case "collins"   => Source(Collins)
      case "mw"        => Source(MerriamWebster)
    }
    .withDefault(Source(MerriamWebster))

val checkSynonyms =
  Opts.subcommand("check", "Check if the given words are synonyms") {
    (words, source).mapN((t, s) => CheckSynonyms(t._1, t._2, s))
  }

val listSynonyms =
  Opts.subcommand("list", "List synonyms for a word") {
    (Opts.argument[String]("word"), source).mapN(ListSynonyms.apply)
  }
