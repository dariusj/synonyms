package synonyms

import cats.effect.*
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import synonyms.core.config.Config
import synonyms.core.programs.Synonyms
import synonyms.core.resources.{MkHttpClient, ThesaurusClients}
import synonyms.core.services.ThesaurusService
import synonyms.modules.HttpApi
import synonyms.resources.MkHttpServer

object SynonymsHttp extends IOApp.Simple:

  given SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  override val run: IO[Unit] =
    Config.load[IO].flatMap { cfg =>
      val serverR = for
        httpClient       <- MkHttpClient[IO].newEmber(cfg.httpClientConfig)
        thesaurusClients <- ThesaurusClients.make(httpClient)
        thesaurusService = ThesaurusService.make(thesaurusClients)
        synonyms         = Synonyms(thesaurusService, cfg.synonymConfig)
        httpApp          = HttpApi.make(synonyms, cfg.thesaurusConfig).httpApp
        server <- MkHttpServer[IO].newEmber(cfg.httpServerConfig, httpApp)
      yield server
      serverR.useForever
    }
