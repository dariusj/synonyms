//> using dep net.ruippeixotog::scala-scraper:3.1.1
//> using dep org.typelevel::cats-core::2.10.0
//> using dep org.typelevel::cats-effect:3.5.4

package synonyms

import cats.effect.*
import cats.syntax.show.*
import synonyms.thesaurus.Found
import synonyms.thesaurus.Service
import synonyms.thesaurus.algebra.Thesaurus
import synonyms.thesaurus.interpreter.MerriamWebster

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.given
import scala.util.Failure
import scala.util.Success

object Synonyms extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    def parseArgs(seq: List[String]): IO[(String, String)] = seq match
      case f :: s :: Nil => IO.pure(f -> s)
      case args =>
        IO.raiseError(
          new IllegalArgumentException(s"Incorrect number of arguments: $args")
        )

    given Thesaurus[IO] = MerriamWebster
    parseArgs(args)
      .flatMap { case (first, second) =>
        Service
          .checkSynonyms[IO](first, second)
          .flatMap(result => IO.println(result.show))
      }
      .as(ExitCode.Success)
