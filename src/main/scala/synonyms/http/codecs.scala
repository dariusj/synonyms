package synonyms.http

import cats.syntax.validated.*
import io.circe.*
import org.http4s.*
import org.http4s.dsl.io.*
import synonyms.domain.*

given QueryParamDecoder[Thesaurus] =
  QueryParamDecoder[String].emap(s =>
    Thesaurus
      .fromString(s)
      .toRight(ParseFailure(s"Unsupported thesaurus $s", ""))
  )

given QueryParamDecoder[Word] = QueryParamDecoder[String].map(Word.apply)

object ThesaurusParamMatcher
    extends OptionalMultiQueryParamDecoderMatcher[Thesaurus](
      "thesaurus"
    )

object WordsMatcher extends OptionalMultiQueryParamDecoderMatcher[Word]("word")

given Encoder[Definition]    = Encoder.encodeString.contramap(_.toString)
given Encoder[Example]       = Encoder.encodeString.contramap(_.toString)
given Encoder[ThesaurusName] = Encoder.encodeString.contramap(_.toString)
given Encoder[Word]          = Encoder.encodeString.contramap(_.toString)
