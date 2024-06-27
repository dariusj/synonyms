package synonyms.routes

import cats.data.Validated.*
import cats.data.ValidatedNel
import cats.effect.*
import cats.syntax.apply.*
import cats.syntax.show.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.headers.Accept
import synonyms.server.{*, given}
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.response.{Result, SynonymsByLength}

object Routes:
  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> Root / "synonyms" / WordVar(
          word
        ) :? ThesaurusParamMatcher(
          thesaurusesValidated: ValidatedNel[ParseFailure, List[Client[IO]]]
        ) =>
      def getEntries[A](f: List[SynonymsByLength] => A)(using
          EntityEncoder[IO, A]
      ): IO[Response[IO]] =
        thesaurusesValidated.withDefault match
          case Valid(thesauruses) =>
            Service()
              .getEntries2(word, thesauruses)
              .map(SynonymsByLength.fromEntries)
              .foldF(_ => InternalServerError(), syns => Ok(f(syns)))
          case Invalid(e) => BadRequest(e.map(_.sanitized).asJson)

      req.headers.get[Accept] match
        case Some(value) if value.isJson => getEntries(_.asJson)
        case Some(value) if value.isText => getEntries(_.show)
        case Some(value) => BadRequest(s"Unsupported Accept header: $value")
        case None        => getEntries(_.asJson)

    case req @ GET -> Root / "synonyms" :? WordsMatcher(
          words
        ) +& ThesaurusParamMatcher(thesaurusesValidated) =>
      def checkSynonyms[F[_], A](f: Result => A)(using EntityEncoder[F, A]) =
        val validated =
          (thesaurusesValidated.withDefault, words.toTuple2).mapN {
            case (thesauruses, (first, second)) =>
              Service().checkSynonyms2(first, second, thesauruses)
          }
        validated match
          case Valid(result) =>
            result.foldF(_ => InternalServerError(), f => Ok(f.asJson))
          case Invalid(e) => BadRequest(e.map(_.sanitized).asJson)

      req.headers.get[Accept] match
        case Some(value) if value.isJson => checkSynonyms(_.asJson)
        case Some(value) if value.isText => checkSynonyms(_.show)
        case Some(value) => BadRequest(s"Unsupported Accept header: $value")
        case None        => checkSynonyms(_.asJson)
  }
