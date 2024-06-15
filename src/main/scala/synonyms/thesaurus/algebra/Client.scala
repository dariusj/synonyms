package synonyms.thesaurus.algebra

import synonyms.thesaurus.*
import scala.util.control.NoStackTrace
import synonyms.thesaurus.algebra.Client.*

trait Client[F[_]]:
  type Doc

  def name: ThesaurusName

  def fetchDocument(word: String): F[Either[FetchError, Option[Doc]]]
  def buildEntries(word: String, document: Doc): List[Entry]

object Client:
  sealed trait FetchError                                 extends NoStackTrace
  final case class ClientError(word: String, url: String) extends FetchError
