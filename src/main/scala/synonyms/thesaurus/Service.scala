package synonyms.thesaurus

import cats.*
import cats.data.EitherT
import cats.syntax.parallel.*
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.algebra.Client.FetchError
import synonyms.thesaurus.response.Result
import synonyms.thesaurus.response.Result.*

class Service[F[_]: Monad: Parallel]:

  def checkSynonyms2(
      first: Word,
      second: Word,
      clients: List[Client[F]]
  ): EitherT[F, FetchError, Result] =
    clients
      .parTraverse(c => checkSynonyms(first, second, c))
      .map(f => f.reduce(_ combine _))

  def getEntries2(
      word: Word,
      clients: List[Client[F]]
  ): EitherT[F, FetchError, List[Entry]] =
    clients.parFlatTraverse(client => getEntries(word, client))

  def getEntries(
      word: Word,
      client: Client[F]
  ): EitherT[F, FetchError, List[Entry]] =
    EitherT(client.fetchDocument(word)).semiflatMap(doc =>
      doc.fold(Applicative[F].pure(Nil))(d => client.buildEntries(word, d))
    )

  def checkSynonyms(
      first: Word,
      second: Word,
      client: Client[F]
  ): EitherT[F, FetchError, Result] =
    def areSynonyms(
        word: Word,
        candidate: Word,
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
