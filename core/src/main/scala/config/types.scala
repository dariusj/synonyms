package synonyms.core.config

import cats.data.NonEmptyList
import synonyms.core.domain.{CharacterSet, SynonymLength, Thesaurus}

import scala.concurrent.duration.FiniteDuration

object types:
  case class HttpClientConfig(timeout: FiniteDuration)
  case class ThesaurusConfig(default: NonEmptyList[Thesaurus])
  case class SynonymConfig(characterSet: CharacterSet, maxLength: SynonymLength)
