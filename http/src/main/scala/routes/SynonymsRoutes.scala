package synonyms.http.routes

import cats.MonadThrow
import cats.data.Validated.*
import cats.data.ValidatedNel
import cats.syntax.apply.*
import cats.syntax.flatMap.*
import io.circe.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.*
import org.http4s.headers.Accept
import org.http4s.server.Router
import synonyms.core.config.types.*
import synonyms.core.domain.*
import synonyms.core.programs.*
import synonyms.http.*

case class SynonymsRoutes[F[_]: MonadThrow](synonyms: Synonyms[F], thesaurusConfig: ThesaurusConfig)
    extends Http4sDsl[F]:
  private val prefixPath = "/synonyms"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ GET -> Root / WordVar(word) :? ThesaurusParamMatcher(
          thesaurusesValidated: ValidatedNel[ParseFailure, List[Thesaurus]]
        ) =>
      def getEntries[A](using
          Transformable[List[SynonymsByLength], A],
          EntityEncoder[F, A]
      ): F[Response[F]] =
        thesaurusesValidated.withDefault(thesaurusConfig.default.toList) match
          case Valid(thesauruses) =>
            synonyms.synonymsByLength(word, thesauruses).flatMap(s => Ok(s.toEntity))
          case Invalid(e) => BadRequest(e.map(_.sanitized).asJson)

      req.headers.get[Accept] match
        case Some(value) if value.isJson => getEntries[Json]
        case Some(value) if value.isText => getEntries[String]
        case Some(value)                 => BadRequest(s"Unsupported Accept header: $value")
        case None                        => getEntries[Json]

    case req @ GET -> Root :? WordParamsMatcher(words) +& ThesaurusParamMatcher(
          thesaurusesValidated
        ) =>
      def checkSynonyms[A](using Transformable[Result, A], EntityEncoder[F, A]) =
        val validated =
          (thesaurusesValidated.withDefault(thesaurusConfig.default.toList), words.toTuple2).mapN {
            case (thesauruses, (first, second)) =>
              synonyms.checkSynonyms(first, second, thesauruses)
          }
        validated match
          case Valid(result) => result.flatMap(result => Ok(result.toEntity))
          case Invalid(e)    => BadRequest(e.map(_.sanitized).asJson)

      req.headers.get[Accept] match
        case Some(value) if value.isJson => checkSynonyms[Json]
        case Some(value) if value.isText => checkSynonyms[String]
        case Some(value)                 => BadRequest(s"Unsupported Accept header: $value")
        case None                        => checkSynonyms[Json]
  }

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
