package synonyms.core.programs

import cats.*
import cats.effect.MonadCancelThrow
import cats.syntax.functor.*
import cats.syntax.parallel.*
import synonyms.core.domain.*
import synonyms.core.domain.Result.*
import synonyms.core.services.ThesaurusService

final class Synonyms[F[_]: MonadCancelThrow: Parallel](service: ThesaurusService[F]):
  def synonymsByLength(word: Word, thesauruses: List[Thesaurus]): F[List[SynonymsByLength]] =
    thesauruses
      .parFlatTraverse(thesaurus => service.getEntries(word, thesaurus))
      .map(SynonymsByLength.fromEntries)

  def checkSynonyms(first: Word, second: Word, thesauruses: List[Thesaurus]): F[Result] =
    thesauruses
      .parTraverse(thesaurus => checkSynonymsSingle(first, second, thesaurus))
      .map(results => results.reduce(_ combine _))

  private def checkSynonymsSingle(first: Word, second: Word, thesaurus: Thesaurus): F[Result] =
    def areSynonyms(word: Word, candidate: Word, entries: List[Entry]): Result =
      entries.foldLeft[Result](NotSynonyms(word, candidate)) {
        case (_: NotSynonyms, entry) => entry.hasSynonym(candidate)
        case (found: AreSynonyms, _) => found
      }

    def getAndCheck(word1: Word, word2: Word): F[Result] =
      service.getEntries(word1, thesaurus).map(entries => areSynonyms(word1, word2, entries))

    (getAndCheck(first, second), getAndCheck(second, first)).parMapN(_ combine _)
