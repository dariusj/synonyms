package synonyms.thesaurus.algebra

import synonyms.thesaurus.*

trait Client[F[_]]:
  type Doc

  def name: ThesaurusName

  def fetchDocument(word: String): F[Doc]
  def buildEntries(word: String, document: Doc): List[Entry]
