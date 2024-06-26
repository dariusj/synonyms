package synonyms

import cats.data.OptionT
import cats.effect.*
import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.{ErrorAction, ErrorHandling}
import synonyms.routes.Routes.service

object SynonymsApi extends IOApp.Simple:
  val httpApp: Http[IO, IO] = Router(
    "/" -> withErrorLogging(service)
  ).orNotFound

  val serverBuilder: BlazeServerBuilder[IO] =
    BlazeServerBuilder[IO].withHttpApp(httpApp).bindHttp(host = "0.0.0.0")

  def errorHandler(t: Throwable, msg: => String): OptionT[IO, Unit] =
    OptionT.liftF(
      IO.println(msg) >>
        IO.println(t) >>
        IO(t.printStackTrace())
    )

  def withErrorLogging(errorRoute: HttpRoutes[IO]) =
    ErrorHandling.Recover.total(
      ErrorAction.log(
        errorRoute,
        messageFailureLogAction = errorHandler,
        serviceErrorLogAction = errorHandler
      )
    )

  override val run: IO[Unit] = serverBuilder.resource.useForever
