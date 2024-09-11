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
