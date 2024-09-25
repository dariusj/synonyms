package synonyms.resources

import cats.effect.{Async, Resource}
import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server
import synonyms.config.types.HttpServerConfig

trait MkHttpServer[F[_]]:
  def newBlaze(cfg: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server]

object MkHttpServer:
  def apply[F[_]: MkHttpServer]: MkHttpServer[F] = implicitly

  implicit def forAsync[F[_]: Async]: MkHttpServer[F] = new MkHttpServer[F] {
    def newBlaze(cfg: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server] =
      BlazeServerBuilder[F]
        .bindHttp(cfg.port, cfg.host)
        .withHttpApp(httpApp)
        .resource
  }
