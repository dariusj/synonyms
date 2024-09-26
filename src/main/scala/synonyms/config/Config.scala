package synonyms.config

import cats.syntax.apply.*
import com.comcast.ip4s.{Host, Port}
import synonyms.config.types.*
import synonyms.domain.*

import scala.concurrent.duration.given

object Config:
  private val httpClientConfig = HttpClientConfig(30.seconds)
  private val httpServerConfig =
    (Host.fromString("0.0.0.0"), Port.fromInt(8080)).mapN(HttpServerConfig.apply)
  private val thesaurusConfig = ThesaurusConfig(Thesaurus.all)

  def loadForHttp     = AppConfig(thesaurusConfig, httpClientConfig, httpServerConfig)
  def load: AppConfig = AppConfig(thesaurusConfig, httpClientConfig, None)
