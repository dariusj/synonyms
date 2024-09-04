//> using option -Wunused:all
//> using option -Ykind-projector:underscores
//> using option -deprecation

//> using dep co.fs2::fs2-core:3.10.2
//> using dep com.monovore::decline-effect::2.4.1
//> using dep io.circe::circe-core::0.14.9
//> using dep io.circe::circe-generic::0.14.9
//> using dep io.circe::circe-parser::0.14.9
//> using dep net.ruippeixotog::scala-scraper:3.1.1
//> using dep org.gnieh::fs2-data-json-circe::1.11.0
//> using dep org.gnieh::fs2-data-json::1.11.0
//> using dep org.http4s::http4s-blaze-client:0.23.16
//> using dep org.http4s::http4s-blaze-server:0.23.16
//> using dep org.http4s::http4s-circe:0.23.27
//> using dep org.http4s::http4s-dsl:0.23.27
//> using dep org.http4s::http4s-ember-client:0.23.27
//> using dep org.typelevel::cats-core::2.12.0
//> using dep org.typelevel::cats-effect:3.5.4

//> using test.dep co.fs2::fs2-io:3.10.2
//> using test.dep org.scalameta::munit-scalacheck::1.0.0
//> using test.dep org.scalameta::munit::1.0.0
//> using test.dep org.typelevel::munit-cats-effect::2.0.0

package synonyms

import cats.effect.*
import cats.syntax.show.*
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import io.circe.generic.auto.*
import io.circe.syntax.*
import synonyms.CliArgs.*
import synonyms.thesaurus.*
import synonyms.thesaurus.response.{Result, SynonymsByLength}

object SynonymsCli
    extends CommandIOApp(
      "synonyms",
      "An application to check synonyms",
      true,
      "v0.1"
    ):

  val args: CliArgs[IO] = CliArgs[IO]

  def main: Opts[IO[ExitCode]] = (args.checkSynonyms orElse args.listSynonyms)
    .map {
      case CheckSynonyms.Args(first, second, clients, format) =>
        Service()
          .checkSynonyms2(first, second, clients.toList)
          .map { result =>
            format match
              case Format.Json => result.asJson.toString
              case Format.Text => result.show
          }

      case ListSynonyms.Args(word, clients, format) =>
        Service()
          .getEntries2(word, clients.toList)
          .map(entries =>
            val synonyms = SynonymsByLength.fromEntries(entries)
            format match
              case Format.Json => synonyms.asJson.toString
              case Format.Text => synonyms.map(_.show).mkString("\n")
          )
    }
    .map(_.flatMap(IO.println) *> IO(ExitCode.Success))
