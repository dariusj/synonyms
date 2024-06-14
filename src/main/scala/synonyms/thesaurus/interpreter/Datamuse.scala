package synonyms.thesaurus.interpreter

import _root_.io.circe.*
import _root_.io.circe.generic.semiauto.*
import cats.effect.IO
import cats.syntax.option.*
import fs2.*
import fs2.data.json.*
import fs2.data.json.circe.*
import fs2.data.json.codec.*
import org.http4s.*
import org.http4s.ember.client.*
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.algebra.Client.ClientError
import synonyms.thesaurus.{Entry, ThesaurusName}

import scala.annotation.tailrec

object Datamuse extends Client[IO]:
  type Doc = List[DatamuseWord]

  def url(word: String): Uri =
    Uri.unsafeFromString(s"https://api.datamuse.com/words?ml=$word")

  def request(word: String): Request[IO] = Request[IO](Method.GET, url(word))

  override def name: ThesaurusName = ThesaurusName("Datamuse")

  override def fetchDocument(word: String): IO[Either[ClientError, Doc]] =
    val words = for {
      client <- Stream.resource(EmberClientBuilder.default[IO].build)
      res    <- client.stream(request(word))
      body <- res.body
        .through(text.utf8.decode)
        .through(tokens)
        .through(deserialize[IO, List[DatamuseWord]])
    } yield body
    words.compile.toList.map(l => Right(l.flatten))

  override def buildEntries(
      word: String,
      document: List[DatamuseWord]
  ): List[Entry] =
    DatamuseWord.toEntries(word, document)

  final case class DatamuseWord(word: String, tags: List[String])

  object DatamuseWord:
    given Decoder[DatamuseWord] = deriveDecoder[DatamuseWord]
    def toEntries(
        word: String,
        datamuseWords: List[DatamuseWord]
    ): List[Entry] =

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
            val wordsByPos = word.tags.collect {
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

//  enum PartOfSpeech:
//    case Noun, Verb, Adjective, Adverb, Other
//
