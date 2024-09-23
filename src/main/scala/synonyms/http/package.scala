package synonyms.http

import cats.Show
import cats.data.ValidatedNel
import cats.syntax.show.*
import cats.syntax.validated.*
import io.circe.Encoder
import io.circe.Json
import io.circe.syntax.*
import org.http4s.*
import org.http4s.headers.Accept
import synonyms.domain.*

type PfValidated[A] = ValidatedNel[ParseFailure, A]

extension (v: PfValidated[List[Thesaurus]])
  def withDefault(default: List[Thesaurus]): ValidatedNel[ParseFailure, List[Thesaurus]] = v.map {
    case Nil  => default
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

trait Transformable[A, B]:
  extension (a: A) def toEntity: B

object Transformable:
  given [A: Encoder, B]: Transformable[A, Json] with
    extension (a: A) def toEntity: Json = a.asJson

  given [A: Show]: Transformable[A, String] with
    extension (a: A) def toEntity: String = a.show
