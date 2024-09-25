package synonyms.config

import cats.data.NonEmptyList
import synonyms.domain.Thesaurus

object types:
  // TODO: Better types
  case class HttpServerConfig(host: String, port: Int)
  case class ThesaurusConfig(default: NonEmptyList[Thesaurus])
  case class AppConfig(thesaurusConfig: ThesaurusConfig, httpServerConfig: Option[HttpServerConfig])
