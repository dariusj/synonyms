package synonyms

import cats.FlatMap
import cats.effect.Sync
import cats.syntax.flatMap.given
import cats.syntax.functor.given

object Service:
  def getEntries[F[_]: FlatMap](
      word: String
  )(using thesaurus: Thesaurus[F]): F[List[Entry]] =
    for
      document <- thesaurus.fetchDocument(word)
      entries = thesaurus.buildEntries(word, document)
    yield entries

  def checkSynonyms[F[_]: FlatMap: Sync](first: String, second: String)(using
      thesaurus: Thesaurus[F]
  ): F[Boolean] =
    for {
      firstEntries <- getEntries(first)
      firstSynonyms = firstEntries.flatMap(_.entryItems.flatMap(_.synonyms))
      _ <- Sync[F].delay(
        println(s"Synonyms of $first: ${firstSynonyms.mkString(", ")}")
      )
      secondEntries <- getEntries(second)
      secondSynonyms = firstEntries.flatMap(_.entryItems.flatMap(_.synonyms))
      _ <- Sync[F].delay(
        println(s"Synonyms of $second: ${secondSynonyms.mkString(", ")}")
      )
    } yield firstSynonyms.contains(second) || secondSynonyms.contains(first)
