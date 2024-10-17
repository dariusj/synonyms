package synonyms.cli

import org.scalacheck.Gen
import synonyms.core.PropHelpers.*
import synonyms.core.domain.Thesaurus
import synonyms.core.domain.Thesaurus.*

object PropHelpers:
  val formatGen: Gen[Format] = Gen.oneOf(Format.values.toList)
  val thesaurusWithKeyGen: Gen[(Thesaurus, String)] = thesaurusGen.map { thesaurus =>
    val key = thesaurus match
      case Cambridge      => "cambridge"
      case Collins        => "collins"
      case Datamuse       => "datamuse"
      case MerriamWebster => "merriamwebster"
      case PowerThesaurus => "powerthesaurus"
      case WordHippo      => "wordhippo"
    thesaurus -> key
  }
