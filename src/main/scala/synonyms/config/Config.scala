package synonyms.config

import cats.syntax.apply.*
import com.comcast.ip4s.{Host, Port}
import synonyms.config.types.*
import synonyms.domain.*

object Config:
  private val thesaurusConfig = ThesaurusConfig(Thesaurus.all)
  private val httpServerConfig =
    (Host.fromString("0.0.0.0"), Port.fromInt(8080)).mapN(HttpServerConfig.apply)

  def loadForHttp()     = AppConfig(thesaurusConfig, httpServerConfig)
  def load(): AppConfig = AppConfig(thesaurusConfig, None)
