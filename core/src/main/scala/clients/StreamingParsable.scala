package synonyms.core.clients

import cats.syntax.option.*
import fs2.*
import fs2.data.json.*
import fs2.data.json.circe.*
import fs2.data.json.codec.*
import monocle.syntax.all.*
import synonyms.core.domain.*
import synonyms.core.domain.Thesaurus.Datamuse

trait StreamingParsable[F[_], T]:
  def parseDocument(word: Word, document: Stream[F, Byte]): Stream[F, Entry]

object StreamingParsable:
  given [F[_]: RaiseThrowable]: StreamingParsable[F, Datamuse] with
    given ThesaurusName                              = Datamuse.name
    val toPos: PartialFunction[String, PartOfSpeech] = {
      case "adj" => PartOfSpeech.Adjective
      case "adv" => PartOfSpeech.Adverb
      case "n"   => PartOfSpeech.Noun
      case "u"   => PartOfSpeech.Undetermined
      case "v"   => PartOfSpeech.Verb
    }

    private case class EntryAcc(word: Word, underlying: Map[PartOfSpeech, Entry]):
      def addWord(dw: Datamuse.Word, pos: PartOfSpeech): EntryAcc =
        val newUnderlying = underlying.updated(
          pos,
          underlying.get(pos) match
            case None        => Entry(Datamuse.name, word, pos, None, None, List(Synonym(dw.word)))
            case Some(entry) => entry.focus(_.synonyms).modify(_ :+ Synonym(dw.word))
        )
        EntryAcc(word, newUnderlying)

    def parseDocument(word: Word, document: Stream[F, Byte]): Stream[F, Entry] =
      document
        .through(text.utf8.decode)
        .through(tokens)
        // TODO: This blocks until the whole list is deserialized. Do this incrementally
        .through(deserialize[F, List[Datamuse.Word]])
        .flatMap(Stream.emits)
        .fold(EntryAcc(word, Map.empty)) { case (acc, word) =>
          val partsOfSpeech = word.tags.orEmpty.collect(toPos)
          partsOfSpeech.foldLeft(acc) { case (a, pos) => a.addWord(word, pos) }
        }
        .flatMap(acc => Stream.emits(acc.underlying.values.toList))
