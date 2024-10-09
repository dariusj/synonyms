package synonyms.cli.config

import synonyms.cli.config.types.*
import synonyms.core.config.types.*
import synonyms.core.domain.{CharacterSet, Thesaurus}

import scala.concurrent.duration.given

object Config:
  private val httpClientConfig = HttpClientConfig(30.seconds)
  private val thesaurusConfig  = ThesaurusConfig(Thesaurus.all)
  private val synonymConfig    = SynonymConfig(CharacterSet.Alphabetic, 15)

  def load: AppConfig = AppConfig(thesaurusConfig, httpClientConfig, synonymConfig)
