package synonyms.core

import org.scalacheck.{Arbitrary, Gen}
import synonyms.core.domain.*
import synonyms.core.domain.Result.{AreSynonyms, NotSynonyms}

object PropHelpers:
  def nonEmptyStringGen(
      maxLength: Int = 15,
      char: Gen[Char] = Gen.alphaChar
  ): Gen[String] =
    Gen.resize(maxLength, Gen.nonEmptyStringOf(char))

  def nonEmptyListGen[A](a: Gen[A], maxLength: Int = 5): Gen[List[A]] =
    Gen.resize(maxLength, Gen.nonEmptyListOf(a))

  val thesaurusNameGen: Gen[ThesaurusName] =
    nonEmptyStringGen().map(ThesaurusName.apply)

  val wordGen: Gen[Word] = nonEmptyStringGen().map(Word.applyUnsafe)

  val partOfSpeechGen: Gen[PartOfSpeech] = Gen.oneOf(PartOfSpeech.values.toList)

  val definitionGen: Gen[Definition] = nonEmptyListGen(nonEmptyStringGen())
    .map(_.mkString(" "))
    .map(Definition.apply)

  val exampleGen: Gen[Example] = nonEmptyStringGen().map(Example.apply)

  val synonymGen: Gen[Synonym] = nonEmptyStringGen().map(Synonym.apply)

  val entryGen: Gen[Entry] = for
    thesaurusName <- thesaurusNameGen
    word          <- wordGen
    partOfSpeech  <- partOfSpeechGen
    definition    <- Gen.option(definitionGen)
    example       <- Gen.option(exampleGen)
    synonyms      <- Gen.nonEmptyListOf(synonymGen)
  yield Entry(thesaurusName, word, partOfSpeech, definition, example, synonyms)

  val thesaurusGen: Gen[Thesaurus] = Gen.oneOf(Thesaurus.all.toList)

  val notSynonymsGen: Gen[NotSynonyms] = for
    firstWord  <- wordGen
    secondWord <- wordGen
  yield NotSynonyms(firstWord, secondWord)

  val areSynonymsGen: Gen[AreSynonyms] = for
    firstWord    <- wordGen
    secondWord   <- wordGen
    partOfSpeech <- partOfSpeechGen
    definition   <- Gen.option(definitionGen)
    example      <- Gen.option(exampleGen)
    source       <- thesaurusNameGen
  yield AreSynonyms(
    firstWord,
    secondWord,
    partOfSpeech,
    definition,
    example,
    source
  )

  val resultGen: Gen[Result] = Gen.oneOf(areSynonymsGen, notSynonymsGen)

  given Arbitrary[Entry] = Arbitrary(entryGen)
