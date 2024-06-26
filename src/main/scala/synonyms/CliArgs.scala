package synonyms

import cats.syntax.validated.given
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.effect.IO
import cats.syntax.apply.given
import com.monovore.decline.{Argument, Opts}
import synonyms.thesaurus.Word
import synonyms.thesaurus.algebra.Client

object CliArgs:
  val clientOpts: Opts[NonEmptyList[Client[IO]]] =
    Opts
      .options[String]("source", "The thesaurus to use", "s")
      .mapValidated(_.traverse { input =>
        Validated
          .fromOption(Client.fromString(input), s"Invalid source $input")
          .toValidatedNel
      })
      .withDefault(Client.allClients)

  given Argument[Word] with
    override def read(string: String): ValidatedNel[String, Word] = Word(
      string
    ).validNel
    override def defaultMetavar: String = "word"

  object CheckSynonyms:
    final case class Args[F[_]](
        first: Word,
        second: Word,
        source: NonEmptyList[Client[F]]
    )
    
    val words: Opts[(Word, Word)] = Opts.arguments[Word]("words").map {
      case NonEmptyList(f, s :: Nil) => f -> s
      case args =>
        throw IllegalArgumentException(s"Incorrect number of arguments: $args")
    }

  object ListSynonyms:
    final case class Args[F[_]](word: Word, source: NonEmptyList[Client[F]])

  val checkSynonyms =
    Opts.subcommand("check", "Check if the given words are synonyms") {
      (CheckSynonyms.words, clientOpts).mapN { case ((word1, word2), clients) =>
        CheckSynonyms.Args(word1, word2, clients)
      }
    }

  val listSynonyms =
    Opts.subcommand("list", "List synonyms for a word") {
      (Opts.argument[Word]("word"), clientOpts).mapN(ListSynonyms.Args.apply)
    }
