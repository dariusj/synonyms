package synonyms.routes

import io.circe.syntax.*
import cats.data.Kleisli
import cats.effect.*
import io.circe.Encoder
import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.io.*
import org.http4s.server.Router
import synonyms.thesaurus.{Entry, Service, ThesaurusName}
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityEncoder.*

object Routes:
  given Encoder[ThesaurusName] = Encoder.encodeString.contramap(_.toString)

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "synonyms" / word =>
      Ok(
        Service(synonyms.thesaurus.interpreter.Datamuse)
          .getEntries(word)
          .map(Entry.synonymsByLength)
          .value
          .map(_.toOption.get)
      )
  }
