package synonyms.clients

import cats.Applicative
import cats.syntax.option.*
import synonyms.domain.*
import synonyms.domain.Thesaurus.Datamuse

import scala.annotation.tailrec

trait JsonParsable[F[_], T]:
  def parseDocument(word: Word, document: List[Datamuse.Word]): F[List[Entry]]

object JsonParsable:
  given [F[_]: Applicative]: JsonParsable[F, Datamuse] with
    extension (s: String)
      def toPos: PartOfSpeech = s match
        case "adj" => PartOfSpeech.Adjective
        case "adv" => PartOfSpeech.Adverb
        case "n"   => PartOfSpeech.Noun
        case "u"   => PartOfSpeech.Undetermined
        case "v"   => PartOfSpeech.Verb

    private def toEntries(
        word: Word,
        datamuseWords: List[Datamuse.Word]
    ): List[Entry] =
      @tailrec
      def rec(
          a: Map[String, List[Datamuse.Word]],
          words: List[(String, Datamuse.Word)]
      ): Map[String, List[Datamuse.Word]] =
        words match
          case (pos, w) :: tail =>
            rec(a.updated(pos, a.get(pos).orEmpty :+ w), tail)
          case Nil => a

      val wordLookups =
        datamuseWords.foldLeft(Map.empty[String, List[Datamuse.Word]]) {
          case (acc, word) =>
            val wordsByPos = word.tags.orEmpty.collect {
              case tag @ ("n" | "v" | "adj" | "adv" | "u") => tag -> word
            }
            rec(acc, wordsByPos)
        }
      wordLookups.map { (pos, words) =>
        Entry(
          Datamuse.name,
          word,
          pos.toPos,
          None,
          None,
          words.map(dw => Word(dw.word))
        )
      }.toList

    def parseDocument(
        word: Word,
        document: List[Datamuse.Word]
    ): F[List[Entry]] =
      Applicative[F].pure(toEntries(word, document))
