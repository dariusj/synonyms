package synonyms.config

import synonyms.domain.Thesaurus
import cats.data.NonEmptyList

object types:
  case class ThesaurusConfig(default: NonEmptyList[Thesaurus])
  case class AppConfig(thesaurusConfig: ThesaurusConfig)
