package synonyms.thesaurus.algebra

import synonyms.thesaurus.*
import scala.util.control.NoStackTrace
import synonyms.thesaurus.algebra.Client.*

trait Client[F[_]]:
  type Doc

  def name: ThesaurusName

  def fetchDocument(word: String): F[Either[ClientError, Doc]]
  def buildEntries(word: String, document: Doc): List[Entry]

object Client:
  sealed trait ClientError extends NoStackTrace
  final case class NotFound(word: String, url: String) extends ClientError