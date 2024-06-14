package synonyms.thesaurus

import cats.FlatMap
import cats.syntax.flatMap.given
import cats.syntax.foldable.*
import cats.syntax.functor.given
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Thesaurus

object Service:
  def getEntries[F[_]: FlatMap](
      word: String
  )(using thesaurus: Thesaurus[F]): F[List[Entry]] =
    for
      document <- thesaurus.fetchDocument(word)
      entries = thesaurus.buildEntries(word, document)
    yield entries

  def checkSynonyms[F[_]: FlatMap](first: String, second: String)(using
      thesaurus: Thesaurus[F]
  ): F[Option[Result]] =
    def isSynonym(word: String, candidate: String): F[Option[Result]] =
      getEntries[F](word).map(entries =>
        entries.collectFirstSome(_.synonym(candidate))
      )

    for
      firstResult <- isSynonym(first, second)
      secondResult <- isSynonym(second, first)
    yield firstResult.orElse(secondResult)
