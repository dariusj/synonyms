package synonyms.core.config

import cats.data.{NonEmptyChain, Validated, ValidatedNec}
import cats.syntax.apply.*
import cats.syntax.option.*
import cats.syntax.semigroup.*
import cats.syntax.validated.*
import cats.{ApplicativeThrow, Semigroup}
import com.comcast.ip4s.{Host, Port}
import synonyms.core.config.types.*
import synonyms.core.domain.Thesaurus
import synonyms.http.config.types.*

import scala.concurrent.duration.given
import scala.util.control.NoStackTrace

object Config:
  private def httpServerConfig: ValidatedNec[ConfigError, HttpServerConfig] =
    (
      Host.fromString("0.0.0.0").toValidNec(ConfigError("Invalid host")),
      Port.fromInt(8080).toValidNec(ConfigError("Invalid port"))
    ).mapN(HttpServerConfig.apply)

  def load[F[_]: ApplicativeThrow]: F[AppConfig] =
    val validatedConfig: ValidatedNec[ConfigError, AppConfig] = (
      ThesaurusConfig(Thesaurus.all).validNec,
      HttpClientConfig(30.seconds).validNec,
      httpServerConfig
    ).mapN { case (thesaurusConfig, httpClientConfig, httpServerConfig) =>
      AppConfig(thesaurusConfig, httpClientConfig, httpServerConfig)
    }
    validatedConfig.leftMap(_.reduceLeft(_ combine _)).liftTo[F]

  case class ConfigError(errors: NonEmptyChain[String]) extends NoStackTrace

  object ConfigError:
    def apply(string: String): ConfigError = ConfigError(NonEmptyChain.one(string))
    given Semigroup[ConfigError] with
      def combine(x: ConfigError, y: ConfigError): ConfigError = ConfigError(
        x.errors concat y.errors
      )
