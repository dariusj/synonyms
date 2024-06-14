package synonyms.server

import cats.effect.*
import cats.syntax.validated.*
import org.http4s.*
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.interpreter.*

object QueryParamDecoders:
  given QueryParamDecoder[Client[IO]] =
    QueryParamDecoder[String].emapValidatedNel {
      case "cambridge" => Cambridge.validNel
      case "collins"   => Collins.validNel
      case "datamuse"  => Datamuse.validNel
      case "mw"        => MerriamWebster.validNel
      case thesaurus =>
        ParseFailure(s"Unsupported thesaurus $thesaurus", "").invalidNel
    }
