package synonyms.thesaurus.interpreter

import _root_.io.circe.*
import _root_.io.circe.generic.semiauto.*
import cats.effect.IO
import cats.syntax.option.*
import org.http4s.*
import synonyms.thesaurus.*
import synonyms.thesaurus.interpreter.Datamuse.DatamuseWord

import scala.annotation.tailrec

object Datamuse extends JsonApi[List[DatamuseWord], IO]:

  def url(word: Word): Uri =
    Uri.unsafeFromString(s"https://api.datamuse.com/words?ml=$word")

  override def name: ThesaurusName = ThesaurusName("Datamuse")

  override def buildEntries(
      word: Word,
      document: List[DatamuseWord]
  ): List[Entry] =
    DatamuseWord.toEntries(word, document)

  final case class DatamuseWord(word: String, tags: Option[List[String]])

  object DatamuseWord:
    given Decoder[DatamuseWord] = deriveDecoder[DatamuseWord]
    def toEntries(word: Word, datamuseWords: List[DatamuseWord]): List[Entry] =

      @tailrec
      def rec(
          a: Map[String, List[DatamuseWord]],
          words: List[(String, DatamuseWord)]
      ): Map[String, List[DatamuseWord]] =
        words match
          case (pos, w) :: tail =>
            rec(a.updated(pos, a.get(pos).orEmpty :+ w), tail)
          case Nil => a

      val wordLookups =
        datamuseWords.foldLeft(Map.empty[String, List[DatamuseWord]]) {
          case (acc, word) =>
            val wordsByPos = word.tags.orEmpty.collect {
              case tag @ ("n" | "v" | "adj" | "adv" | "u") => tag -> word
            }
            rec(acc, wordsByPos)
        }
      wordLookups.map { (pos, words) =>
        Entry(
          ThesaurusName("Datamuse"),
          word,
          pos,
          None,
          None,
          words.map(_.word)
        )
      }.toList
