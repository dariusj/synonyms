package synonyms.routes

import cats.Apply
import cats.data.Validated.*
import cats.data.{NonEmptyList, ValidatedNel}
import cats.effect.*
import cats.syntax.validated.*
import io.circe.Encoder
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*
import synonyms.server.QueryParamDecoders.given
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.interpreter.*
import synonyms.thesaurus.{Entry, Service, SynonymsByLength, ThesaurusName}

object Routes:
  object ThesaurusParamMatcher
      extends OptionalValidatingQueryParamDecoderMatcher[Client[IO]](
        "thesaurus"
      )
  object WordsMatcher
      extends OptionalMultiQueryParamDecoderMatcher[String]("words")

  given Encoder[ThesaurusName] = Encoder.encodeString.contramap(_.toString)

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "synonyms" / word :? ThesaurusParamMatcher(
          thesaurusValidated
        ) =>
      thesaurusValidated.getOrElse(Datamuse.validNel) match
        case Valid(thesaurus) => listRoute(word, thesaurus)
        case Invalid(e)       => BadRequest(e.map(_.sanitized))

    case GET -> Root / "synonyms" :? WordsMatcher(
          words
        ) +& ThesaurusParamMatcher(thesaurusValidated) =>
      val validated = Apply[ValidatedNel[ParseFailure, _]]
        .map2(thesaurusValidated.getOrElse(Datamuse.validNel), words) {
          case (thesaurus, first :: second :: Nil) =>
            Service()
              .checkSynonyms(first, second, thesaurus)
              .foldF(_ => NotFound(), Ok(_))
          case (_, list) => BadRequest(list.mkString("\n"))
        }
      validated match
        case Valid(response) => response
        case Invalid(e)      => BadRequest(e.map(_.sanitized))
  }

  def listRoute(word: String, thesaurus: Client[IO]): IO[Response[IO]] =
    Service()
      .getEntries(word, thesaurus)
      .map(SynonymsByLength.fromEntries)
      .foldF(_ => NotFound(), Ok(_))
