package synonyms.cli

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.syntax.apply.given
import cats.syntax.validated.given
import com.monovore.decline.{Argument, Opts}
import synonyms.domain.*

private val sourceOpts: Opts[NonEmptyList[Thesaurus]] =
  Opts
    .options[String]("source", "The thesaurus to use", "s")
    .mapValidated(_.traverse { input =>
      Validated
        .fromOption(Thesaurus.fromString(input), s"Invalid source $input")
        .toValidatedNel
    })
    .withDefault(Thesaurus.all)

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

val checkSynonyms: Opts[CheckSynonyms.Args] =
  Opts.subcommand("check", "Check if the given words are synonyms") {
    (CheckSynonyms.words, sourceOpts, formatOpts).mapN {
      case ((word1, word2), thesauruses, formatOpts) =>
        CheckSynonyms.Args(word1, word2, thesauruses, formatOpts)
    }
  }

val listSynonyms: Opts[ListSynonyms.Args] =
  Opts.subcommand("list", "List synonyms for a word") {
    (Opts.argument[Word]("word"), sourceOpts, formatOpts)
      .mapN(ListSynonyms.Args.apply)
  }

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
  case class Args(word: Word, source: NonEmptyList[Thesaurus], format: Format)

object CheckSynonyms:
  case class Args(
      first: Word,
      second: Word,
      source: NonEmptyList[Thesaurus],
      format: Format
  )

  val words: Opts[(Word, Word)] = Opts.arguments[Word]("words").map {
    case NonEmptyList(f, s :: Nil) => f -> s
    case args =>
      throw IllegalArgumentException(s"Incorrect number of arguments: $args")
  }
