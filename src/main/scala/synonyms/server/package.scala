package synonyms.server

import cats.data.ValidatedNel
import cats.effect.*
import cats.syntax.validated.*
import io.circe.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.Accept
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client

given QueryParamDecoder[Client[IO]] =
  QueryParamDecoder[String].emap(s =>
    Client
      .fromString(s)
      .toRight(ParseFailure(s"Unsupported thesaurus $s", ""))
  )

given QueryParamDecoder[Word] = QueryParamDecoder[String].map(Word.apply)

object ThesaurusParamMatcher
    extends OptionalMultiQueryParamDecoderMatcher[Client[IO]](
      "thesaurus"
    )

object WordsMatcher extends OptionalMultiQueryParamDecoderMatcher[Word]("word")

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
          "Must pass two 'word' arguments only",
          list.mkString("\n")
        ).invalidNel
    }

extension (acceptHeader: Accept)
  def satisfiedBy(range: MediaRange): Boolean =
    acceptHeader.values.exists(_.mediaRange.satisfiedBy(range))
  def isJson: Boolean = acceptHeader.satisfiedBy(MediaType.application.json)
  def isText: Boolean = acceptHeader.satisfiedBy(MediaType.text.plain)

object WordVar:
  def unapply(str: String): Option[Word] =
    Option.when(str.nonEmpty)(Word(str))
