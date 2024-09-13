package synonyms.http.routes

import cats.MonadThrow
import cats.data.Validated.*
import cats.data.ValidatedNel
import cats.syntax.apply.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.*
import org.http4s.headers.Accept
import org.http4s.server.Router
import synonyms.domain.*
import synonyms.http.*
import synonyms.services.*

final case class SynonymsRoutes[F[_]: MonadThrow](service: Synonyms[F])
    extends Http4sDsl[F]:
  private val prefixPath = "/synonyms"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ GET -> Root / WordVar(
          word
        ) :? ThesaurusParamMatcher(
          thesaurusesValidated: ValidatedNel[ParseFailure, List[Thesaurus]]
        ) =>
      def getEntries[A](using
          ee: EntityEncoder[F, A],
          transformable: Transformable[List[SynonymsByLength], A]
      ): F[Response[F]] =
        thesaurusesValidated.withDefault match
          case Valid(thesauruses) =>
            service
              .getEntries2(word, thesauruses)
              .map(SynonymsByLength.fromEntries)
              .flatMap(s => Ok(transformable.toEntity(s)))
          case Invalid(e) => BadRequest(e.map(_.sanitized).asJson)

      req.headers.get[Accept] match
        case Some(value) if value.isJson => getEntries[Json]
        case Some(value) if value.isText => getEntries[String]
        case Some(value) => BadRequest(s"Unsupported Accept header: $value")
        case None        => getEntries[Json]

    case req @ GET -> Root :? WordsMatcher(
          words
        ) +& ThesaurusParamMatcher(thesaurusesValidated) =>
      def checkSynonyms[A](using
          ee: EntityEncoder[F, A],
          transformable: Transformable[Result, A]
      ) =
        val validated =
          (thesaurusesValidated.withDefault, words.toTuple2).mapN {
            case (thesauruses, (first, second)) =>
              service.checkSynonyms2(first, second, thesauruses)
          }
        validated match
          case Valid(result) =>
            result.flatMap(result => Ok(transformable.toEntity(result)))
          case Invalid(e) => BadRequest(e.map(_.sanitized).asJson)

      req.headers.get[Accept] match
        case Some(value) if value.isJson => checkSynonyms[Json]
        case Some(value) if value.isText => checkSynonyms[String]
        case Some(value) => BadRequest(s"Unsupported Accept header: $value")
        case None        => checkSynonyms[Json]
  }

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
