package synonyms.routes

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
import synonyms.thesaurus.{Entry, Service, SynonymsByLength}

object Routes:
  object ThesaurusParamMatcher
      extends OptionalValidatingQueryParamDecoderMatcher[Client[IO]](
        "thesaurus"
      )

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "synonyms" / word :? ThesaurusParamMatcher(
          thesaurusValidated
        ) =>
      def evaluateWord(thesaurus: Client[IO]): IO[Response[IO]] =
        Service(thesaurus)
          .getEntries(word)
          .map(SynonymsByLength.fromEntries)
          .foldF(_ => NotFound(), Ok(_))

      thesaurusValidated
        .getOrElse(Datamuse.validNel)
        .fold(
          parseFailures => BadRequest(parseFailures.map(_.sanitized)),
          thesaurus => evaluateWord(thesaurus)
        )
  }
