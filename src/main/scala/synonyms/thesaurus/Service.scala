package synonyms.thesaurus

import cats.Monad
import cats.data.EitherT
import synonyms.thesaurus.*
import synonyms.thesaurus.Result.*
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.algebra.Client.ClientError

class Service[F[_]: Monad](client: Client[F]):

  def getEntries(word: String): EitherT[F, ClientError, List[Entry]] =
    for document <- EitherT(client.fetchDocument(word))
    yield client.buildEntries(word, document)

  def checkSynonyms(
      first: String,
      second: String
  ): EitherT[F, ClientError, Result] =
    def areSynonyms(
        word: String,
        candidate: String
    ): EitherT[F, ClientError, Result] =
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
