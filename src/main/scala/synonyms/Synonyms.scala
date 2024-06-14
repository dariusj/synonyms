//> using option -Wunused:all

//> using dep com.monovore::decline-effect::2.4.1
//> using dep net.ruippeixotog::scala-scraper:3.1.1
//> using dep org.typelevel::cats-core::2.12.0
//> using dep org.typelevel::cats-effect:3.5.4

//> using test.dep org.scalameta::munit::1.0.0
//> using test.dep org.scalameta::munit-scalacheck::1.0.0

package synonyms

import cats.effect.*
import cats.syntax.show.*
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import synonyms.thesaurus.*
import synonyms.thesaurus.Service
import synonyms.thesaurus.algebra.Thesaurus

import CliArgs.*

object Synonyms
    extends CommandIOApp(
      "synonyms",
      "An application to check synonyms",
      true,
      "v0.1"
    ):
  def main: Opts[IO[ExitCode]] = (checkSynonyms orElse listSynonyms)
    .map {
      case CheckSynonyms.Args(first, second, source) =>
        given Thesaurus[IO] = source.source
        Service
          .checkSynonyms[IO](first, second)
          .map(_.show)
      case ListSynonyms.Args(word, source) =>
        given Thesaurus[IO] = source.source
        Service
          .getEntries[IO](word)
          .map(entries =>
            Entry
              .synonymsByLength(entries)
              .map { case (l, words) => s"($l) ${words.mkString(", ")}" }
              .mkString("\n")
          )
    }
    .map(output => output.flatMap(IO.println).as(ExitCode.Success))
