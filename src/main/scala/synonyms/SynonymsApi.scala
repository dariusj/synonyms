package synonyms

import cats.effect.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import synonyms.config.Config
import synonyms.modules.HttpApi
import synonyms.resources.{MkHttpServer, ThesaurusClients}
import synonyms.services.Synonyms

object SynonymsApi extends IOApp.Simple:

  given Logger[IO] = Slf4jLogger.getLogger[IO]

  override val run: IO[Unit] =
    val cfg = Config.loadForHttp()
    ThesaurusClients
      .make[IO]
      .map { clients =>
        val service = Synonyms.make(clients)
        HttpApi.make(service, cfg).httpApp
      }
      .flatMap { httpApp =>
        MkHttpServer[IO].newEmber(cfg.httpServerConfig.get, httpApp)
      }
      .useForever
