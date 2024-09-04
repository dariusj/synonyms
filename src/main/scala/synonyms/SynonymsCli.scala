package synonyms

import cats.effect.*
import cats.syntax.show.*
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import io.circe.generic.auto.*
import io.circe.syntax.*
import synonyms.cli.*
import synonyms.domain.Result
import synonyms.domain.SynonymsByLength
import synonyms.modules.ThesaurusClients
import synonyms.services.*

object SynonymsCli
    extends CommandIOApp(
      "synonyms",
      "An application to check synonyms",
      true,
      "v0.1"
    ):

  val clientsR = ThesaurusClients.make[IO]
  val service  = Synonyms.make(clientsR)
  def main: Opts[IO[ExitCode]] = (checkSynonyms orElse listSynonyms)
    .map {
      case CheckSynonyms.Args(first, second, thesauruses, format) =>
        service
          .checkSynonyms2(first, second, thesauruses.toList)
          .map { result =>
            format match
              case Format.Json => result.asJson.toString
              case Format.Text => result.show
          }

      case ListSynonyms.Args(word, thesauruses, format) =>
        service
          .getEntries2(word, thesauruses.toList)
          .map { entries =>
            val synonyms = SynonymsByLength.fromEntries(entries)
            format match
              case Format.Json => synonyms.asJson.toString
              case Format.Text => synonyms.map(_.show).mkString("\n")
          }
    }
    .map(_.flatMap(IO.println) *> IO(ExitCode.Success))
