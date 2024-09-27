package synonyms

import cats.effect.*
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import synonyms.config.Config
import synonyms.modules.HttpApi
import synonyms.resources.{MkHttpClient, MkHttpServer, ThesaurusClients}
import synonyms.services.Synonyms

object SynonymsApi extends IOApp.Simple:

  given SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  override val run: IO[Unit] =
    val cfg = Config.loadForHttp
    val serverR = for
      httpClient       <- MkHttpClient[IO].newEmber(cfg.httpClientConfig)
      thesaurusClients <- ThesaurusClients.make(httpClient)
      synonymsService = Synonyms.make(thesaurusClients)
      httpApp         = HttpApi.make(synonymsService, cfg.thesaurusConfig).httpApp
      server <- MkHttpServer[IO].newEmber(cfg.httpServerConfig.get, httpApp)
    yield server
    serverR.useForever
