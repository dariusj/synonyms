package synonyms.http.config

import com.comcast.ip4s.{Host, Port}
import synonyms.core.config.types.{HttpClientConfig, ThesaurusConfig}

object types:
  case class HttpServerConfig(host: Host, port: Port)
  case class AppConfig(
      thesaurusConfig: ThesaurusConfig,
      httpClientConfig: HttpClientConfig,
      httpServerConfig: HttpServerConfig
  )
