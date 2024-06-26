package synonyms.thesaurus.algebra

import cats.data.NonEmptyList
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client.*

import scala.util.control.NoStackTrace

trait Client[F[_]]:
  type Doc

  def name: ThesaurusName

  def fetchDocument(word: Word): F[Either[FetchError, Option[Doc]]]
  def buildEntries(word: Word, document: Doc): List[Entry]

object Client:
  sealed trait FetchError                                 extends NoStackTrace
  final case class ClientError(word: String, url: String) extends FetchError

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
