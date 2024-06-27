package synonyms

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.syntax.apply.given
import cats.syntax.validated.given
import com.monovore.decline.{Argument, Opts}
import synonyms.CliArgs.{CheckSynonyms, Format, ListSynonyms, given}
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.{ClientProvider, Word}

class CliArgs[F[_]: ClientProvider]:
  private val clientOpts: Opts[NonEmptyList[Client[F]]] =
    Opts
      .options[String]("source", "The thesaurus to use", "s")
      .mapValidated(_.traverse { input =>
        Validated
          .fromOption(Client.fromString[F](input), s"Invalid source $input")
          .toValidatedNel
      })
      .withDefault(Client.allClients[F])

  private val formatOpts: Opts[Format] =
    Opts
      .option[String]("format", "The result format", "f")
      .mapValidated { format =>
        Validated
          .fromOption(
            Format.fromString(format),
            s"$format is not a valid format"
          )
          .toValidatedNel
      }
      .withDefault(Format.Text)

  val checkSynonyms: Opts[CheckSynonyms.Args[F]] =
    Opts.subcommand("check", "Check if the given words are synonyms") {
      (CheckSynonyms.words, clientOpts, formatOpts).mapN {
        case ((word1, word2), clients, formatOpts) =>
          CheckSynonyms.Args(word1, word2, clients, formatOpts)
      }
    }

  val listSynonyms: Opts[ListSynonyms.Args[F]] =
    Opts.subcommand("list", "List synonyms for a word") {
      (Opts.argument[Word]("word"), clientOpts, formatOpts)
        .mapN(ListSynonyms.Args.apply)
    }

object CliArgs:
  enum Format:
    case Json, Text

  object Format:
    private val pf: PartialFunction[String, Format] = {
      case "json" => Json
      case "text" => Text
    }
    def fromString(s: String): Option[Format] = pf.lift(s)

  given Argument[Word] with
    override def read(string: String): ValidatedNel[String, Word] = Word(
      string
    ).validNel
    override def defaultMetavar: String = "word"

  object ListSynonyms:
    case class Args[F[_]](
        word: Word,
        source: NonEmptyList[Client[F]],
        format: Format
    )

  object CheckSynonyms:
    case class Args[F[_]](
        first: Word,
        second: Word,
        source: NonEmptyList[Client[F]],
        format: Format
    )

    val words: Opts[(Word, Word)] = Opts.arguments[Word]("words").map {
      case NonEmptyList(f, s :: Nil) => f -> s
      case args =>
        throw IllegalArgumentException(s"Incorrect number of arguments: $args")
    }
