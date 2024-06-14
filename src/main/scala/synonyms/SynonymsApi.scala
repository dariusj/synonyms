package synonyms

import cats.data.Kleisli
import cats.effect.*
import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.io.*
import org.http4s.server.Router
import synonyms.routes.Routes.service

object SynonymsApi extends IOApp.Simple:
  val httpApp: Kleisli[IO, Request[IO], Response[IO]] = Router("/" -> service).orNotFound

  val serverBuilder: BlazeServerBuilder[IO] = BlazeServerBuilder[IO].withHttpApp(httpApp)

  override val run: IO[Unit] = serverBuilder.resource.useForever
