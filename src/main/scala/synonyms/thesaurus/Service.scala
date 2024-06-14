package synonyms.thesaurus

import cats.FlatMap
import cats.syntax.flatMap.given
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
  ): F[Result] =
    def areSynonyms(word: String, candidate: String): F[Result] =
      getEntries[F](word).map(entries =>
        entries.foldLeft[Result](NotFound(word, candidate)) {
          case (_: NotFound, entry) => entry.hasSynonym(candidate)
          case (found: Found, _)    => found
        }
      )

    for
      firstResult <- areSynonyms(first, second)
      secondResult <- areSynonyms(second, first)
    yield firstResult.combine(secondResult)

  // def checkSynonyms[F[_]]: FlatMap](first: String, second: String)(thesauri: List[Thesaurus[F]]): F[Result]