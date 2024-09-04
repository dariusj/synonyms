package synonyms

import org.scalacheck.{Arbitrary, Gen}
import synonyms.domain.*

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

  val partOfSpeechGen: Gen[PartOfSpeech] = Gen.oneOf(PartOfSpeech.values.toList)

  val definitionGen: Gen[Definition] = nonEmptyListGen(nonEmptyStringGen())
    .map(_.mkString(" "))
    .map(Definition.apply)

  val exampleGen: Gen[Example] = nonEmptyStringGen().map(Example.apply)

  val entryGen: Gen[Entry] = for
    thesaurusName <- thesaurusNameGen
    word          <- wordGen
    partOfSpeech  <- partOfSpeechGen
    definition    <- Gen.option(definitionGen)
    example       <- Gen.option(exampleGen)
    synonyms      <- Gen.listOf(wordGen)
  yield Entry(thesaurusName, word, partOfSpeech, definition, example, synonyms)

  val thesaurusGen: Gen[Thesaurus] = Gen.oneOf(Thesaurus.all.toList)

  given Arbitrary[Entry] = Arbitrary(entryGen)
