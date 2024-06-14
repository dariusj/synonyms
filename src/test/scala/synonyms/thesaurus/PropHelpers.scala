package synonyms.thesaurus

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

object PropHelpers:
  def nonEmptyStringGen(maxLength: Int = 15, char: Gen[Char] = Gen.alphaChar) =
    Gen.resize(15, Gen.nonEmptyStringOf(Gen.alphaChar))

  def nonEmptyListGen[A](el: Gen[A], maxLength: Int = 5) =
    Gen.resize(maxLength, Gen.nonEmptyListOf(el))

  val thesaurusNameGen: Gen[ThesaurusName] =
    nonEmptyStringGen().map(ThesaurusName.apply)

  val phraseGen: Gen[String] =
    nonEmptyListGen(nonEmptyStringGen()).map(_.mkString(" "))

  val entryGen = for {
    thesaurusName <- thesaurusNameGen
    word          <- nonEmptyStringGen()
    partOfSpeech  <- nonEmptyStringGen()
    definition    <- Gen.option(phraseGen)
    example       <- nonEmptyStringGen()
    synonyms      <- Gen.listOf(nonEmptyStringGen())
  } yield Entry(
    thesaurusName,
    word,
    partOfSpeech,
    definition,
    example,
    synonyms
  )

  given Arbitrary[Entry] = Arbitrary(entryGen)
