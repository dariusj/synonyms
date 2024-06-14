//> using dep com.lihaoyi::upickle:3.3.1
//> using dep net.ruippeixotog::scala-scraper:3.1.1
//> using dep org.typelevel::cats-core::2.10.0
//> using dep org.typelevel::cats-effect:3.5.4

package synonyms

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.given
import scala.util.Failure
import scala.util.Success
import cats.syntax.all.*
import cats.effect.*

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
        Service.checkSynonyms[IO](first, second).flatMap { isSynonym =>
          if (isSynonym)
            IO.println(s"$first and $second are synonyms")
          else
            IO.println(s"$first and $second are not synonyms")
        }
      }
      .as(ExitCode.Success)
