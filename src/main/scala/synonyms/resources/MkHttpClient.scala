package synonyms.resources

import cats.effect.{Async, Resource}
import fs2.io.net.Network
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import synonyms.config.types.HttpClientConfig

trait MkHttpClient[F[_]]:
  def newEmber(cfg: HttpClientConfig): Resource[F, Client[F]]

object MkHttpClient:
  def apply[F[_]: MkHttpClient]: MkHttpClient[F] = summon

  given forAsync[F[_]: Async: Network]: MkHttpClient[F] = new MkHttpClient[F]:
    def newEmber(cfg: HttpClientConfig): Resource[F, Client[F]] =
      EmberClientBuilder.default[F].withTimeout(cfg.timeout).build
