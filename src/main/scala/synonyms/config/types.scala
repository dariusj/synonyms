package synonyms.config

import cats.data.NonEmptyList
import com.comcast.ip4s.{Host, Port}
import synonyms.domain.Thesaurus

object types:
  case class HttpServerConfig(host: Host, port: Port)
  case class ThesaurusConfig(default: NonEmptyList[Thesaurus])
  case class AppConfig(thesaurusConfig: ThesaurusConfig, httpServerConfig: Option[HttpServerConfig])
