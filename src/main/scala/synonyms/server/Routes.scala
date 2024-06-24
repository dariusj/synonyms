package synonyms.routes

import cats.data.Validated.*
import cats.data.{EitherT, NonEmptyList, Validated, ValidatedNel}
import cats.effect.*
import cats.syntax.apply.*
import cats.syntax.validated.*
import io.circe.Encoder
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*
import synonyms.server.QueryParamDecoders.given
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client

object Routes:
  object ThesaurusParamMatcher
      extends OptionalMultiQueryParamDecoderMatcher[Client[IO]](
        "thesaurus"
      )
  object WordsMatcher
      extends OptionalMultiQueryParamDecoderMatcher[String]("words")

  given Encoder[ThesaurusName] = Encoder.encodeString.contramap(_.toString)

  type PfValidated[A] = ValidatedNel[ParseFailure, A]

  extension (v: PfValidated[List[Client[IO]]])
    def withDefault: ValidatedNel[ParseFailure, List[Client[IO]]] = v.map {
      case Nil  => Client.allClients.toList
      case list => list
    }

  extension (v: PfValidated[List[String]])
    def toTuple2: ValidatedNel[ParseFailure, (String, String)] =
      v.andThen {
        case first :: second :: Nil => (first, second).validNel
        case list =>
          ParseFailure(
            "Must pass two 'words' arguments only",
            list.mkString("\n")
          ).invalidNel
      }

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "synonyms" / word :? ThesaurusParamMatcher(
          thesaurusesValidated
        ) =>
      thesaurusesValidated.withDefault match
        case Valid(thesauruses) =>
          Service()
            .getEntries2(word, thesauruses)
            .map(SynonymsByLength.fromEntries)
            .foldF(_ => InternalServerError(), Ok(_))
        case Invalid(e) => BadRequest(e.map(_.sanitized))

    case GET -> Root / "synonyms" :? WordsMatcher(
          words
        ) +& ThesaurusParamMatcher(thesaurusesValidated) =>
      val validated = (thesaurusesValidated.withDefault, words.toTuple2).mapN {
        case (thesauruses, (first, second)) =>
          Service().checkSynonyms2(first, second, thesauruses)
      }
      validated match
        case Valid(result) => result.foldF(_ => InternalServerError(), Ok(_))
        case Invalid(e)    => BadRequest(e.map(_.sanitized))
  }
