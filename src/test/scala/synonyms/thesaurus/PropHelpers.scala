package synonyms.thesaurus

import org.scalacheck.{Arbitrary, Gen}

object PropHelpers:
  def nonEmptyStringGen(
      maxLength: Int = 15,
      char: Gen[Char] = Gen.alphaChar
  ): Gen[String] =
    Gen.resize(15, Gen.nonEmptyStringOf(Gen.alphaChar))

  def nonEmptyListGen[A](el: Gen[A], maxLength: Int = 5): Gen[List[A]] =
    Gen.resize(maxLength, Gen.nonEmptyListOf(el))

  val thesaurusNameGen: Gen[ThesaurusName] =
    nonEmptyStringGen().map(ThesaurusName.apply)

  val wordGen: Gen[Word] = nonEmptyStringGen().map(Word.apply)

  val phraseGen: Gen[String] =
    nonEmptyListGen(nonEmptyStringGen()).map(_.mkString(" "))

  val entryGen: Gen[Entry] = for
    thesaurusName <- thesaurusNameGen
    word          <- wordGen
    partOfSpeech  <- nonEmptyStringGen()
    definition    <- Gen.option(phraseGen)
    example       <- Gen.option(nonEmptyStringGen())
    synonyms      <- Gen.listOf(wordGen)
  yield Entry(thesaurusName, word, partOfSpeech, definition, example, synonyms)

  given Arbitrary[Entry] = Arbitrary(entryGen)
