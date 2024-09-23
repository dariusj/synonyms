package synonyms.config

import synonyms.config.types.*
import synonyms.domain.*

object Config:
  def load(): AppConfig = AppConfig(ThesaurusConfig(Thesaurus.all))
