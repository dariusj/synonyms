package synonyms

import cats.data.OptionT
import cats.effect.*
import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.middleware.ErrorHandling
import synonyms.http.routes.SynonymsRoutes
import synonyms.modules.ThesaurusClients
import synonyms.services.Synonyms
import synonyms.config.Config

object SynonymsApi extends IOApp.Simple:
  def errorHandler(t: Throwable, msg: => String): OptionT[IO, Unit] =
    OptionT.liftF(IO.println(msg) >> IO.println(t) >> IO(t.printStackTrace()))

  def withErrorLogging(errorRoute: HttpRoutes[IO]) =
    ErrorHandling.Recover.total(
      ErrorAction.log(
        errorRoute,
        messageFailureLogAction = errorHandler,
        serviceErrorLogAction = errorHandler
      )
    )

  override val run: IO[Unit] =
    IO(Config.load()).flatMap { cfg =>
      ThesaurusClients
        .make[IO]
        .evalMap { clients =>
          IO(Synonyms.make(clients)).map { service =>
            val httpApp: HttpApp[IO] = Router(
              "/" -> withErrorLogging(SynonymsRoutes(service, cfg.thesaurusConfig).routes)
            ).orNotFound
            httpApp
          }
        }
        .flatMap { httpApp =>
          BlazeServerBuilder[IO].withHttpApp(httpApp).bindHttp(host = "0.0.0.0").resource
        }
        .useForever
    }
