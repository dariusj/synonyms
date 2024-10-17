package synonyms.cli

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.syntax.apply.given
import cats.syntax.either.*
import cats.syntax.option.*
import com.monovore.decline.{Argument, Opts}
import synonyms.core.config.types.ThesaurusConfig
import synonyms.core.domain.*

private def sourceOpts(defaultThesaurus: NonEmptyList[Thesaurus]): Opts[NonEmptyList[Thesaurus]] =
  Opts
    .options[String]("source", "The thesaurus to use", "s")
    .mapValidated(
      _.traverse(input => Thesaurus.fromString(input).toValidNel(s"Invalid source $input"))
    )
    .withDefault(defaultThesaurus)

private val formatOpts: Opts[Format] =
  Opts
    .option[String]("format", "The result format", "f")
    .mapValidated(f =>
      Validated
        .catchNonFatal(Format.valueOf(f.toLowerCase.capitalize))
        .leftMap(_ => s"Invalid format '$f'")
        .toValidatedNel
    )
    .withDefault(Format.Text)

def checkSynonyms(thesaurusConfig: ThesaurusConfig): Opts[CheckSynonyms.Args] =
  Opts.subcommand("check", "Check if the given words are synonyms") {
    (CheckSynonyms.words, sourceOpts(thesaurusConfig.default), formatOpts).mapN {
      case ((word1, word2), thesauruses, formatOpts) =>
        CheckSynonyms.Args(word1, word2, thesauruses, formatOpts)
    }
  }

def listSynonyms(thesaurusConfig: ThesaurusConfig): Opts[ListSynonyms.Args] =
  Opts.subcommand("list", "List synonyms for a word") {
    (Opts.argument[Word]("word"), sourceOpts(thesaurusConfig.default), formatOpts)
      .mapN(ListSynonyms.Args.apply)
  }

enum Format:
  case Json, Text

given Argument[Word] with
  override def read(string: String): ValidatedNel[String, Word] = Word.either(string).toValidatedNel
  override def defaultMetavar: String                           = "word"

object ListSynonyms:
  case class Args(word: Word, source: NonEmptyList[Thesaurus], format: Format)

object CheckSynonyms:
  case class Args(first: Word, second: Word, source: NonEmptyList[Thesaurus], format: Format)

  val words: Opts[(Word, Word)] = Opts.arguments[Word]("words").map {
    case NonEmptyList(f, s :: Nil) => f -> s
    case args => throw IllegalArgumentException(s"Incorrect number of arguments: $args")
  }
