package synonyms.cli

import cats.data.NonEmptyList
import com.monovore.decline.{Command, Opts}
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*
import synonyms.cli.PropHelpers.*
import synonyms.core.PropHelpers.*
import synonyms.core.domain.Thesaurus

class ArgsSuite extends ScalaCheckSuite:
  property("sourceOpts") {
    forAllNoShrink(
      for
        thesauruses           <- nonEmptyListGen(thesaurusGen)
        maybeThesaurusWithKey <- Gen.option(thesaurusWithKeyGen)
      yield (NonEmptyList.fromListUnsafe(thesauruses.distinct), maybeThesaurusWithKey)
    ) {
      case (thesauruses, Some((thesaurus, thesaurusArg))) =>
        val arg = s"--source=$thesaurusArg"
        assertEquals(
          optsParser(sourceOpts(thesauruses), arg),
          Right(NonEmptyList.one(thesaurus))
        )
      case (thesauruses, None) =>
        assertEquals(optsParser(sourceOpts(thesauruses)), Right(thesauruses))
    }
  }

  test("sourceOpts errors on invalid input") {
    val thesauruses =
      nonEmptyListGen(thesaurusGen).map(t => NonEmptyList.fromListUnsafe(t.distinct)).sample.get
    assert(clue(optsParser(sourceOpts(thesauruses), "--source=foo")).isLeft)
  }

  property("formatOpts") {
    forAllNoShrink(
      for
        format       <- formatGen
        maybeStrings <- Gen.option(List(format.toString.toLowerCase, format.toString.toUpperCase))
      yield (format, maybeStrings)
    ) {
      case (format, Some(strings)) =>
        strings.foreach(string =>
          val arg = s"--format=$string"
          assertEquals(optsParser(formatOpts, arg), Right(format))
        )
      case (format, None) => assert(clue(optsParser(formatOpts)).isRight)
    }
  }

  test("formatOpts errors on invalid input") {
    assert(clue(optsParser(formatOpts, "--format=foo")).isLeft)
  }

  def optsParser[A](opts: Opts[A], args: String*) = Command("", "")(opts).parse(args)
