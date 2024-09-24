package synonyms.config

import cats.data.NonEmptyList
import synonyms.domain.Thesaurus

object types:
  case class ThesaurusConfig(default: NonEmptyList[Thesaurus])
  case class AppConfig(thesaurusConfig: ThesaurusConfig)
