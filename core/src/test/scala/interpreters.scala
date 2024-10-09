package synonyms.core

import cats.effect.IO
import synonyms.core.clients.ThesaurusClient
import synonyms.core.domain.{Entry, Thesaurus, Word}
import synonyms.core.resources.ThesaurusClients

object interpreters:
  class TestThesaurusClients(thesauruses: Map[Thesaurus, ThesaurusClient[IO]])
      extends ThesaurusClients[IO]:
    override def clients: Map[Thesaurus, ThesaurusClient[IO]] = thesauruses

  class TestThesaurusClient(entries: Map[Word, List[Entry]]) extends ThesaurusClient[IO]:
    type Doc = List[Entry]
    override def fetchDocument(word: Word): IO[Option[Doc]] = IO.pure(entries.get(word))
    override def parseDocument(word: Word, document: Doc): IO[List[Entry]] = IO.pure(document)
