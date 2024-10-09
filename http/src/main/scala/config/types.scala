package synonyms.http.config

import cats.Semigroup
import cats.data.NonEmptyChain
import com.comcast.ip4s.{Host, Port}
import synonyms.core.config.types.{HttpClientConfig, SynonymConfig, ThesaurusConfig}

import scala.util.control.NoStackTrace

object types:
  case class HttpServerConfig(host: Host, port: Port)
  case class AppConfig(
      thesaurusConfig: ThesaurusConfig,
      synonymConfig: SynonymConfig,
      httpClientConfig: HttpClientConfig,
      httpServerConfig: HttpServerConfig
  )

  case class ConfigError(errors: NonEmptyChain[String]) extends NoStackTrace

  object ConfigError:
    def apply(string: String): ConfigError = ConfigError(NonEmptyChain.one(string))
    given Semigroup[ConfigError] with
      def combine(x: ConfigError, y: ConfigError): ConfigError = ConfigError(
        x.errors concat y.errors
      )
