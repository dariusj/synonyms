package synonyms

import cats.effect.*
import cats.syntax.show.*
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import io.circe.generic.auto.*
import io.circe.syntax.*
import synonyms.cli.*
import synonyms.config.Config
import synonyms.domain.{Result, SynonymsByLength}
import synonyms.resources.ThesaurusClients
import synonyms.services.*

object SynonymsCli
    extends CommandIOApp(
      "synonyms",
      "An application to check synonyms",
      true,
      "v0.1"
    ):

  def main: Opts[IO[ExitCode]] =
    val cfg = Config.load()
    (checkSynonyms(cfg.thesaurusConfig) orElse listSynonyms(cfg.thesaurusConfig))
      .map { args =>
        ThesaurusClients.make[IO].use { clients =>
          val service: Synonyms[IO] = Synonyms.make(clients)
          val outputIO = args match
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
          outputIO.flatMap(IO.println).as(ExitCode.Success)
        }
      }
