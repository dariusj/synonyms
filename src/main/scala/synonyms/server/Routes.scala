package synonyms.routes

import cats.data.Validated.*
import cats.data.ValidatedNel
import cats.effect.*
import cats.syntax.apply.*
import cats.syntax.show.*
import cats.syntax.validated.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.headers.Accept
import synonyms.server.QueryParamDecoders.given
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.response.{Result, SynonymsByLength}

object Routes:
  object ThesaurusParamMatcher
      extends OptionalMultiQueryParamDecoderMatcher[Client[IO]](
        "thesaurus"
      )
  object WordsMatcher
      extends OptionalMultiQueryParamDecoderMatcher[Word]("words")

  given Encoder[Definition]    = Encoder.encodeString.contramap(_.toString)
  given Encoder[Example]       = Encoder.encodeString.contramap(_.toString)
  given Encoder[ThesaurusName] = Encoder.encodeString.contramap(_.toString)
  given Encoder[Word]          = Encoder.encodeString.contramap(_.toString)

  type PfValidated[A] = ValidatedNel[ParseFailure, A]

  extension (v: PfValidated[List[Client[IO]]])
    def withDefault: ValidatedNel[ParseFailure, List[Client[IO]]] = v.map {
      case Nil  => Client.allClients.toList
      case list => list
    }

  extension (v: PfValidated[List[Word]])
    def toTuple2: ValidatedNel[ParseFailure, (Word, Word)] =
      v.andThen {
        case first :: second :: Nil => (first, second).validNel
        case list =>
          ParseFailure(
            "Must pass two 'words' arguments only",
            list.mkString("\n")
          ).invalidNel
      }

  extension (acceptHeader: Accept)
    def satisfiedBy(range: MediaRange): Boolean =
      acceptHeader.values.exists(_.mediaRange.satisfiedBy(range))
    def isJson: Boolean = acceptHeader.satisfiedBy(MediaType.application.json)
    def isText: Boolean = acceptHeader.satisfiedBy(MediaType.text.plain)

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

  object WordVar:
    def unapply(str: String): Option[Word] =
      Option.when(str.nonEmpty)(Word(str))
