package synonyms

import cats.data.{NonEmptyList, Validated}
import cats.effect.IO
import cats.syntax.apply.given
import com.monovore.decline.Opts
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.interpreter.*

object CliArgs:
  val clientOpts: Opts[NonEmptyList[Client[IO]]] =
    Opts
      .options[String]("source", "The thesaurus to use", "s")
      .mapValidated(_.traverse { input =>
        Validated
          .fromOption(Client.fromString(input), s"Invalid source $input")
          .toValidatedNel
      })
      .withDefault(NonEmptyList.one(MerriamWebster))

  object CheckSynonyms:
    final case class Args[F[_]](
        first: String,
        second: String,
        source: NonEmptyList[Client[F]]
    )
    final case class Words(first: String, second: String)
    val words: Opts[(String, String)] = Opts.arguments[String]("words").map {
      case NonEmptyList(f, s :: Nil) => f -> s
      case args =>
        throw IllegalArgumentException(s"Incorrect number of arguments: $args")
    }

  object ListSynonyms:
    final case class Args[F[_]](word: String, source: NonEmptyList[Client[F]])

  val checkSynonyms =
    Opts.subcommand("check", "Check if the given words are synonyms") {
      (CheckSynonyms.words, clientOpts).mapN { case ((word1, word2), clients) =>
        CheckSynonyms.Args(word1, word2, clients)
      }
    }

  val listSynonyms =
    Opts.subcommand("list", "List synonyms for a word") {
      (Opts.argument[String]("word"), clientOpts).mapN(ListSynonyms.Args.apply)
    }
