package synonyms.thesaurus.algebra

import cats.data.NonEmptyList
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client.*

import scala.util.control.NoStackTrace

trait Client[F[_]]:
  type Doc

  def name: ThesaurusName

  def fetchDocument(word: Word): F[Either[FetchError, Option[Doc]]]
  def buildEntries(word: Word, document: Doc): F[List[Entry]]

object Client:
  sealed trait FetchError                           extends NoStackTrace
  case class ClientError(word: String, url: String) extends FetchError

  sealed class ParseException(message: String)
      extends Exception(message)
      with NoStackTrace
  object ParseException:
    case class PartOfSpeechNotFound(
        pos: String,
        word: Word,
        thesaurus: ThesaurusName
    ) extends ParseException(
          s"[$thesaurus] Unsupported part of speech '$pos' for entry '$word'"
        )

  def allClients[F[_]](using
      provider: ClientProvider[F]
  ): NonEmptyList[Client[F]] =
    NonEmptyList.fromListUnsafe(
      List("cambridge", "datamuse", "mw").flatMap(fromString)
    )

  def fromString[F[_]](string: String)(using
      provider: ClientProvider[F]
  ): Option[Client[F]] =
    val pf: PartialFunction[String, Client[F]] = {
      case "cambridge" => provider.cambridge
      case "collins"   => provider.collins
      case "datamuse"  => provider.datamuse
      case "mw"        => provider.mw
    }
    pf.lift(string)
