package synonyms.cli.config

import synonyms.cli.config.types.*
import synonyms.core.config.types.*
import synonyms.core.domain.Thesaurus

import scala.concurrent.duration.given

object Config:
  private val httpClientConfig = HttpClientConfig(30.seconds)
  private val thesaurusConfig  = ThesaurusConfig(Thesaurus.all)

  def load: AppConfig = AppConfig(thesaurusConfig, httpClientConfig)
