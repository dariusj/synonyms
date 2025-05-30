package synonyms.cli.config

import io.github.iltotore.iron.*
import synonyms.cli.config.types.*
import synonyms.core.config.types.*
import synonyms.core.domain.{CharacterSet, SynonymLength, Thesaurus}

import scala.concurrent.duration.given

object Config:
  private val httpClientConfig = HttpClientConfig(30.seconds)
  private val thesaurusConfig  = ThesaurusConfig(Thesaurus.all)
  private val synonymConfig    = SynonymConfig(CharacterSet.Alphabetic, SynonymLength(15))

  def load: AppConfig = AppConfig(thesaurusConfig, httpClientConfig, synonymConfig)
