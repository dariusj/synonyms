package synonyms.config

import synonyms.config.types.*
import synonyms.domain.*

object Config:
  private val thesaurusConfig  = ThesaurusConfig(Thesaurus.all)
  private val httpServerConfig = HttpServerConfig("0.0.0.0", 8080)

  def loadForHttp()     = AppConfig(thesaurusConfig, Some(httpServerConfig))
  def load(): AppConfig = AppConfig(thesaurusConfig, None)
