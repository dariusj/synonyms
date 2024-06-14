package synonyms.thesaurus

import cats.FlatMap
import cats.syntax.flatMap.given
import cats.syntax.functor.given
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client

class Service[F[_]: FlatMap](client: Client[F]):
  def getEntries(word: String): F[List[Entry]] = for
    document <- client.fetchDocument(word)
    entries = client.buildEntries(word, document)
  yield entries

  def checkSynonyms(first: String, second: String): F[Result] =
    def areSynonyms(word: String, candidate: String): F[Result] =
      getEntries(word).map(entries =>
        entries.foldLeft[Result](NotSynonyms(word, candidate)) {
          case (_: NotSynonyms, entry) => entry.hasSynonym(candidate)
          case (found: AreSynonyms, _) => found
        }
      )

    for
      firstResult  <- areSynonyms(first, second)
      secondResult <- areSynonyms(second, first)
    yield firstResult.combine(secondResult)
