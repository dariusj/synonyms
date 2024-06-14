package synonyms.routes

import cats.data.Validated
import cats.effect.*
import cats.syntax.show.*
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
        case Validated.Valid(thesaurus) => listRoute(word, thesaurus)
        case Validated.Invalid(e)       => BadRequest(e.map(_.sanitized))
    case GET -> Root / "synonyms" :? WordsMatcher(words) =>
      words match
        case Validated.Valid(first :: second :: Nil) =>
          val foo = Service(Datamuse).checkSynonyms(first, second)
          foo.foldF(_ => NotFound(), Ok(_))
        case Validated.Valid(list) =>
          NotFound(list.mkString("\n"))
        case Validated.Invalid(e) => BadRequest(e.map(_.sanitized))
  }

  def listRoute(word: String, thesaurus: Client[IO]): IO[Response[IO]] =
    Service(thesaurus)
      .getEntries(word)
      .map(SynonymsByLength.fromEntries)
      .foldF(_ => NotFound(), Ok(_))
