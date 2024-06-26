package synonyms.server

import cats.effect.*
import org.http4s.*
import synonyms.thesaurus.Word
import synonyms.thesaurus.algebra.Client

object QueryParamDecoders:
  given QueryParamDecoder[Client[IO]] =
    QueryParamDecoder[String].emap(s =>
      Client
        .fromString(s)
        .toRight(ParseFailure(s"Unsupported thesaurus $s", ""))
    )
    
  given QueryParamDecoder[Word] = QueryParamDecoder[String].map(Word.apply)
