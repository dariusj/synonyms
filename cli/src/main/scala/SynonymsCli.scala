package synonyms.cli

import cats.effect.*
import cats.syntax.show.*
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import io.circe.syntax.*
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import synonyms.cli.config.Config
import synonyms.core.domain.{Result, SynonymsByLength}
import synonyms.core.programs.Synonyms
import synonyms.core.resources.{MkHttpClient, ThesaurusClients}
import synonyms.core.services.*

object SynonymsCli
    extends CommandIOApp("synonyms", "An application to check synonyms", true, "v0.1"):

  given SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def main: Opts[IO[ExitCode]] =
    val cfg = Config.load
    (checkSynonyms(cfg.thesaurusConfig) orElse listSynonyms(cfg.thesaurusConfig))
      .map { args =>
        MkHttpClient[IO]
          .newEmber(cfg.httpClientConfig)
          .flatMap(ThesaurusClients.make)
          .use { clients =>
            val service: ThesaurusService[IO] = ThesaurusService.make(clients)
            val synonyms: Synonyms[IO]        = Synonyms(service, cfg.synonymConfig)
            val outputIO: IO[String] = args match
              case CheckSynonyms.Args(first, second, thesauruses, format) =>
                synonyms.checkSynonyms(first, second, thesauruses.toList).map { result =>
                  format match
                    case Format.Json => result.asJson.toString
                    case Format.Text => result.show
                }

              case ListSynonyms.Args(word, thesauruses, format) =>
                synonyms.synonymsByLength(word, thesauruses.toList).map { synonyms =>
                  format match
                    case Format.Json => synonyms.asJson.toString
                    case Format.Text => synonyms.map(_.show).mkString("\n")
                }
            outputIO.flatMap(IO.println).as(ExitCode.Success)
          }
      }
