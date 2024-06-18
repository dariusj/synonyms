package synonyms.thesaurus

import cats.*
import cats.data.EitherT
import cats.syntax.parallel.*
import synonyms.thesaurus.*
import synonyms.thesaurus.Result.*
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.algebra.Client.FetchError

class Service[F[_]: Monad: Parallel]:

  def checkSynonyms2(
      first: String,
      second: String,
      clients: List[Client[F]]
  ): EitherT[F, FetchError, Result] =
    clients
      .parTraverse(c => checkSynonyms(first, second, c))
      .map(f => f.reduce(_ combine _))

  def getEntries2(
      word: String,
      clients: List[Client[F]]
  ): EitherT[F, FetchError, List[Entry]] =
    clients.parFlatTraverse(client => getEntries(word, client))

  def getEntries(
      word: String,
      client: Client[F]
  ): EitherT[F, FetchError, List[Entry]] =
    for maybeDocument <- EitherT(client.fetchDocument(word))
    yield maybeDocument
      .map(doc => client.buildEntries(word, doc))
      .getOrElse(Nil)

  def checkSynonyms(
      first: String,
      second: String,
      client: Client[F]
  ): EitherT[F, FetchError, Result] =
    def areSynonyms(
        word: String,
        candidate: String,
        entries: List[Entry]
    ): Result =
      entries.foldLeft[Result](NotSynonyms(word, candidate)) {
        case (_: NotSynonyms, entry) => entry.hasSynonym(candidate)
        case (found: AreSynonyms, _) => found
      }

    for
      firstEntries <- getEntries(first, client)
      firstResult = areSynonyms(first, second, firstEntries)
      secondEntries <- getEntries(second, client)
      secondResult = areSynonyms(second, first, secondEntries)
    yield firstResult.combine(secondResult)
