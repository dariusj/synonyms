package synonyms.clients

import cats.ApplicativeThrow
import cats.syntax.either.*
import cats.syntax.option.*
import cats.syntax.traverse.*
import synonyms.clients.ParseException.*
import synonyms.domain.*
import synonyms.domain.Thesaurus.Datamuse

import scala.annotation.tailrec

trait JsonParsable[F[_], T]:
  def parseDocument(word: Word, document: List[Datamuse.Word]): F[List[Entry]]

object JsonParsable:
  extension (s: String)
    private def toWord(word: Word)(using thesaurus: ThesaurusName): Either[InvalidSynonym, Word] =
      Word.option(s).toRight(InvalidSynonym(s, word, thesaurus))

  given [F[_]: ApplicativeThrow]: JsonParsable[F, Datamuse] with
    given ThesaurusName = Datamuse.name
    val toPos: PartialFunction[String, PartOfSpeech] = {
      case "adj" => PartOfSpeech.Adjective
      case "adv" => PartOfSpeech.Adverb
      case "n"   => PartOfSpeech.Noun
      case "u"   => PartOfSpeech.Undetermined
      case "v"   => PartOfSpeech.Verb
    }
    private def toEntries(
        word: Word,
        datamuseWords: List[Datamuse.Word]
    ): Either[ParseException, List[Entry]] =
      @tailrec
      def rec(
          a: Map[PartOfSpeech, List[Datamuse.Word]],
          words: List[(PartOfSpeech, Datamuse.Word)]
      ): Map[PartOfSpeech, List[Datamuse.Word]] =
        words match
          case (pos, w) :: tail =>
            rec(a.updated(pos, a.get(pos).orEmpty :+ w), tail)
          case Nil => a

      val wordLookups =
        datamuseWords.foldLeft(Map.empty[PartOfSpeech, List[Datamuse.Word]]) { case (acc, word) =>
          val wordsByPos =
            word.tags.orEmpty.collect(toPos andThen (_ -> word))
          rec(acc, wordsByPos)
        }
      wordLookups
        .map { (pos, words) =>
          words.traverse(dw => dw.word.toWord(word)).map { synonyms =>
            Entry(Datamuse.name, word, pos, None, None, synonyms)
          }
        }
        .toList
        .sequence

    def parseDocument(word: Word, document: List[Datamuse.Word]): F[List[Entry]] =
      toEntries(word, document).liftTo[F]
