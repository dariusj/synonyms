package synonyms.clients

import cats.syntax.functor.*
import cats.{Applicative, Monad}
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.dsl.ToQuery
import net.ruippeixotog.scalascraper.model.Document
import synonyms.domain.*
import synonyms.domain.Thesaurus.Collins

trait HtmlUnitParsable[F[_], T]:
  def parseDocument(word: Word, document: Document): F[List[Entry]]

object HtmlUnitParsable:
  given [F[_]: Monad]: HtmlUnitParsable[F, Collins] with
    given ThesaurusName = Collins.name
    extension (s: String)
      def toPos: PartOfSpeech = s match
        case "(adjective)" => PartOfSpeech.Adjective
        case "(adverb)"  => PartOfSpeech.Adverb

    def parseDocument(word: Word, document: Document): F[List[Entry]] =
      Applicative[F].pure(document).map(_ >> elementList(".entry")).map { entryEls =>
        entryEls.flatMap { el =>
          val senses = el >> elementList(".sense")

          senses.map { sense =>
            val pos = sense >> text(".headerSensePos")
            val definition =
              (sense >?> text(".def")).orElse(sense >?> text(".linkDef"))
            // println(definition)
            val example  = sense >?> text(".type-example")
            val synonyms = sense >> texts(".type-syn .orth")
            Entry(
              Collins.name,
              word,
              pos.toPos,
              definition.map(Definition.apply),
              example.map(Example.apply),
              synonyms.map(Synonym.apply).toList
            )
          }
        }
      }

    //   Applicative[F].pure(document).map(_ >> elementList(".entry")).map { entryEls =>
    //     entryEls.flatMap { el =>
    //       val senses = el >> elementList(".sense")

    //       senses.map { sense =>
    //         val pos = el >> text(".headerSensePos")
    //         val definition =
    //           (el >?> text(".def")).orElse(el >?> text(".linkDef"))
    //         println(definition)
    //         val example  = el >?> text(".type-example")
    //         val synonyms = el >> texts(".type-syn .orth")
    //         Entry(
    //           Collins.name,
    //           word,
    //           pos.toPos,
    //           definition.map(Definition.apply),
    //           example.map(Example.apply),
    //           synonyms.map(Synonym.apply).toList
    //         )
    //       }
    //     }
    //   }
