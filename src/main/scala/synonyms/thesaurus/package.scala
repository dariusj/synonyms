package synonyms.thesaurus

import cats.Show
import synonyms.thesaurus.Result.*

opaque type ThesaurusName = String

object ThesaurusName:
  def apply(value: String): ThesaurusName = value

final case class Entry(
    thesaurusName: ThesaurusName,
    word: String,
    partOfSpeech: String,
    definition: Option[String],
    example: Option[String],
    synonyms: List[String]
):
  def hasSynonym(check: String): Result =
    if synonyms.contains(check) then
      AreSynonyms(word, check, partOfSpeech, definition, example, thesaurusName)
    else NotSynonyms(word, check)

object Entry:
  def synonymsByLength(entries: List[Entry]): List[(Int, List[String])] =
    entries
      .flatMap(_.synonyms)
      .distinct
      .groupBy(_.count(Character.isAlphabetic))
      .toList
      .map { case (k, v) => k -> v.sorted }
      .sorted

final case class SynonymsByLength private (length: Int, synonyms: List[String])

//class Outer[+A](x: A) {
//  class Inner[B >: A](y: B)
//}
//
//object Foo:
//  abstract class Animal:
//    def name: String
//
//  case class Cat(name: String) extends Animal
//  case class Dog(name: String) extends Animal
//
//  class Pets[+A](val pet: A) {
//    def add(pet2: A): String = "done"
//  }
//  val cats               = new Pets[Cat](Cat("Felix"))
//  val pets: Pets[Animal] = cats
//  pets.add(Dog("Fido"))
//
//  abstract class Serializer[-A]:
//    def serialize(a: A): String
//
//  val animalSerializer: Serializer[Animal] = new Serializer[Animal]():
//    def serialize(animal: Animal): String = s"""{ "name": "${animal.name}" }"""
//  animalSerializer.serialize(Cat("Felix"))
//
//  val catSerializer: Serializer[Cat] = animalSerializer
//  catSerializer.serialize(Cat("Felix"))
//
object SynonymsByLength:
  given Show[SynonymsByLength] with
    def show(sbl: SynonymsByLength): String =
      s"(${sbl.length}) ${sbl.synonyms.mkString(", ")}"

  def fromEntries(entries: List[Entry]): List[SynonymsByLength] =
    entries
      .flatMap(_.synonyms)
      .distinct
      .groupBy(_.count(Character.isAlphabetic))
      .toList
      .map { case (k, v) => k -> v.sorted }
      .sorted
      .map(SynonymsByLength.apply.tupled)

sealed abstract class Result:
  def combine(r: Result): Result = (this, r) match
    case (f: AreSynonyms, _) => f
    case (_, f: AreSynonyms) => f
    case _                   => this

object Result:
  given Show[Result] with
    def show(r: Result): String = r match
      case nf: NotSynonyms =>
        s"${nf.firstWord} and ${nf.secondWord} are not synonyms"
      case f: AreSynonyms =>
        import f.*
        s"[Source: $source] $firstWord and $secondWord are synonyms - [$partOfSpeech] '${definition
            .getOrElse("No definition given")}': ${example.getOrElse("No example given")}"

  final case class NotSynonyms(firstWord: String, secondWord: String)
      extends Result

  final case class AreSynonyms(
      firstWord: String,
      secondWord: String,
      partOfSpeech: String,
      definition: Option[String],
      example: Option[String],
      source: ThesaurusName
  ) extends Result
