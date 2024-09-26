package synonyms.config

import cats.data.NonEmptyList
import com.comcast.ip4s.{Host, Port}
import synonyms.domain.Thesaurus

import scala.concurrent.duration.FiniteDuration

object types:
  case class HttpClientConfig(timeout: FiniteDuration)
  case class HttpServerConfig(host: Host, port: Port)
  case class ThesaurusConfig(default: NonEmptyList[Thesaurus])
  case class AppConfig(
      thesaurusConfig: ThesaurusConfig,
      httpClientConfig: HttpClientConfig,
      httpServerConfig: Option[HttpServerConfig]
  )
