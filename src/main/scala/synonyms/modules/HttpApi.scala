package synonyms.modules

import cats.effect.Async
import org.http4s.server.Router
import org.http4s.server.middleware.*
import org.http4s.{HttpApp, HttpRoutes}
import synonyms.config.types.AppConfig
import synonyms.http.routes.SynonymsRoutes
import synonyms.services.Synonyms

sealed abstract class HttpApi[F[_]: Async] private (service: Synonyms[F], cfg: AppConfig):
  private val synonymsRoutes = SynonymsRoutes(service, cfg.thesaurusConfig).routes

  private val routes: HttpRoutes[F] = Router("/" -> synonymsRoutes)

  private val routesMiddleware: HttpRoutes[F] => HttpRoutes[F] = { (http: HttpRoutes[F]) =>
    AutoSlash(http)
  }

  private val appMiddleware: HttpApp[F] => HttpApp[F] = {
    { (http: HttpApp[F]) =>
      RequestLogger.httpApp(logHeaders = true, logBody = true)(http)
    } andThen { (http: HttpApp[F]) => ResponseTiming(http) } andThen { (http: HttpApp[F]) =>
      ResponseLogger.httpApp(logHeaders = true, logBody = true)(http)
    }
  }

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(routes).orNotFound)

object HttpApi:
  def make[F[_]: Async](synonymsService: Synonyms[F], config: AppConfig): HttpApi[F] =
    new HttpApi[F](synonymsService, config) {}
