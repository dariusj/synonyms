//> using dep com.lihaoyi::upickle:3.3.1

final case class Meaning(word: Vector[String])
final case class Entry(word: String, synonyms: Vector[String])
final case class Thesaurus(entries: Vector[Entry])

val (first, second) = args.toList match
  case f :: s :: Nil => f -> s
  case args =>
    println(s"incorrect number of arguments: $args")
    sys.exit(1)

val t = Thesaurus(Vector(Entry("brave", Vector("courageous"))))
def areSynonyms(first: String, second: String): Boolean =
  t.entries.find(_.word == first).exists(_.synonyms.contains(second))

println(areSynonyms(first, second))
