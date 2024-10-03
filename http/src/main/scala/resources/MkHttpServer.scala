package synonyms.resources

import cats.effect.{Async, Resource}
import cats.syntax.show.*
import fs2.io.net.Network
import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import synonyms.http.config.types.HttpServerConfig

trait MkHttpServer[F[_]]:
  def newBlaze(cfg: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server]
  def newEmber(cfg: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server]

object MkHttpServer:
  def apply[F[_]: MkHttpServer]: MkHttpServer[F] = summon

  given forAsync[F[_]: Async: Network]: MkHttpServer[F] = new MkHttpServer[F] {
    def newBlaze(cfg: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server] =
      BlazeServerBuilder[F]
        .bindHttp(cfg.port.show.toInt, cfg.host.show)
        .withHttpApp(httpApp)
        .resource

    def newEmber(cfg: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server] =
      EmberServerBuilder.default[F].withHost(cfg.host).withPort(cfg.port).withHttpApp(httpApp).build
  }
