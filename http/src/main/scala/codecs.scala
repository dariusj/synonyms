package synonyms.http

import cats.syntax.either.*
import org.http4s.*
import org.http4s.dsl.io.*
import synonyms.core.domain.*

given QueryParamDecoder[Thesaurus] =
  QueryParamDecoder[String].emap(s =>
    Thesaurus
      .fromString(s)
      .toRight(ParseFailure(s"Unsupported thesaurus $s", ""))
  )
given QueryParamDecoder[Word] =
  QueryParamDecoder[String].emap(s =>
    Word.either(s).leftMap(error => ParseFailure(s"Invalid word $s: $error", ""))
  )

object ThesaurusParamMatcher extends OptionalMultiQueryParamDecoderMatcher[Thesaurus]("thesaurus")

object WordParamsMatcher extends OptionalMultiQueryParamDecoderMatcher[Word]("word")
