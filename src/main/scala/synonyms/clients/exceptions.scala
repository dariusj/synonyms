package synonyms.clients

import scala.util.control.NoStackTrace
import synonyms.domain.Word
import synonyms.domain.ThesaurusName
import scala.annotation.targetName

sealed class ParseException(message: String) extends Exception(message) with NoStackTrace

object ParseException:
  case class PartOfSpeechNotFound(pos: String, word: Word, thesaurus: ThesaurusName)
      extends ParseException(s"[$thesaurus] Unsupported part of speech '$pos' for entry '$word'")

  object PartOfSpeechNotFound:
    @targetName("applyWithContext")
    def apply(pos: String, word: Word)(using thesaurus: ThesaurusName): PartOfSpeechNotFound =
      PartOfSpeechNotFound(pos, word, thesaurus)

  case class InvalidSynonym(synonym: String, word: Word, thesaurus: ThesaurusName)
      extends ParseException(s"[$thesaurus] Invalid synonym '$synonym' for entry '$word'")

  object InvalidSynonym:
    @targetName("applyWithContext")
    def apply(synonym: String, word: Word)(using thesaurus: ThesaurusName): InvalidSynonym =
      InvalidSynonym(synonym, word, thesaurus)

  case class EntryWithoutPos(word: Word, thesaurus: ThesaurusName)
      extends ParseException(s"[$thesaurus]")

  object EntryWithoutPos:
    @targetName("applyWithContext")
    def apply(word: Word)(using thesaurus: ThesaurusName): EntryWithoutPos =
      EntryWithoutPos(word, thesaurus)
