package synonyms.thesaurus.algebra

import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client.*

import scala.util.control.NoStackTrace

trait Client[F[_]]:
  type Doc

  def name: ThesaurusName

  def fetchDocument(word: String): F[Either[FetchError, Option[Doc]]]
  def buildEntries(word: String, document: Doc): List[Entry]

object Client:
  sealed trait FetchError                                 extends NoStackTrace
  final case class ClientError(word: String, url: String) extends FetchError

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
