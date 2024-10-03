package synonyms.core.config

import cats.data.NonEmptyList
import synonyms.core.domain.Thesaurus

import scala.concurrent.duration.FiniteDuration

object types:
  case class HttpClientConfig(timeout: FiniteDuration)
  case class ThesaurusConfig(default: NonEmptyList[Thesaurus])
