//> using option -Wunused:all

//> using dep net.ruippeixotog::scala-scraper:3.1.1
//> using dep org.typelevel::cats-core::2.10.0
//> using dep org.typelevel::cats-effect:3.5.4
//> using dep dev.optics::monocle-core::3.2.0
//> using dep dev.optics::monocle-macro::3.2.0

package synonyms

import cats.effect.*
import cats.syntax.show.*
import synonyms.thesaurus.Service
import synonyms.thesaurus.algebra.Thesaurus
import synonyms.thesaurus.interpreter.*

object Synonyms extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    def parseArgs(seq: List[String]): IO[(String, String)] = seq match
      case f :: s :: Nil => IO.pure(f -> s)
      case args =>
        IO.raiseError(
          new IllegalArgumentException(s"Incorrect number of arguments: $args")
        )

    given Thesaurus[IO] = Cambridge
    parseArgs(args)
      .flatMap { case (first, second) =>
        Service
          .checkSynonyms[IO](first, second)
          .flatMap(result => IO.println(result.show))
      }
      .as(ExitCode.Success)
