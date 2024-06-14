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
  def main: Opts[IO[ExitCode]] = checkSynonyms.map {
    case CheckSynonyms(words, source) =>
      given Thesaurus[IO] = source.source
      Service
        .checkSynonyms[IO](words.first, words.second)
        .flatMap(result => IO.println(result.show))
        .as(ExitCode.Success)
  }

final case class CheckSynonyms[F[_]](words: Words, source: Source[F])
final case class Source[F[_]](source: Thesaurus[F])
final case class Words(first: String, second: String)
final case class Word(string: String)

val words: Opts[Words] = Opts.arguments[String]("words").map {
  case NonEmptyList(f, s :: Nil) => Words(f, s)
  case args =>
    throw IllegalArgumentException(s"Incorrect number of arguments: $args")
}

val source: Opts[Source[IO]] =
  Opts
    .option[String]("source", "The thesaurus to use", "s")
    .map {
      case "cambridge" => Source(Cambridge)
      case "mw"        => Source(MerriamWebster)
    }
    .withDefault(Source(MerriamWebster))

val checkSynonyms =
  Opts.subcommand("check", "Check if the given words are synonyms") {
    (words, source).mapN(CheckSynonyms.apply)
  }
