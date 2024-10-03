package synonyms.cli.config

import synonyms.core.config.types.*

object types:
  case class AppConfig(thesaurusConfig: ThesaurusConfig, httpClientConfig: HttpClientConfig)
