package synonyms

import cats.data.NonEmptyList
import cats.effect.IO
import cats.syntax.apply.given
import com.monovore.decline.Opts
import synonyms.thesaurus.algebra.Thesaurus
import synonyms.thesaurus.interpreter.*

object CliArgs:
  final case class Source[F[_]](source: Thesaurus[F])

  val source: Opts[Source[IO]] =
    Opts
      .option[String]("source", "The thesaurus to use", "s")
      .map {
        case "cambridge" => Source(Cambridge)
        case "collins"   => Source(Collins)
        case "mw"        => Source(MerriamWebster)
      }
      .withDefault(Source(MerriamWebster))

  object CheckSynonyms:
    final case class Args[F[_]](
        first: String,
        second: String,
        source: Source[F]
    )
    final case class Words(first: String, second: String)
    val words: Opts[(String, String)] = Opts.arguments[String]("words").map {
      case NonEmptyList(f, s :: Nil) => f -> s
      case args =>
        throw IllegalArgumentException(s"Incorrect number of arguments: $args")
    }

  object ListSynonyms:
    final case class Args[F[_]](word: String, source: Source[F])

  val checkSynonyms =
    Opts.subcommand("check", "Check if the given words are synonyms") {
      (CheckSynonyms.words, source).mapN((t, s) =>
        CheckSynonyms.Args(t._1, t._2, s)
      )
    }

  val listSynonyms =
    Opts.subcommand("list", "List synonyms for a word") {
      (Opts.argument[String]("word"), source).mapN(ListSynonyms.Args.apply)
    }
