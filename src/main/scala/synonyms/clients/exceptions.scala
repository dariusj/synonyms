package synonyms.clients

import scala.util.control.NoStackTrace
import synonyms.domain.Word
import synonyms.domain.ThesaurusName

sealed class ParseException(message: String)
    extends Exception(message)
    with NoStackTrace

object ParseException:
  case class PartOfSpeechNotFound(
      pos: String,
      word: Word,
      thesaurus: ThesaurusName
  ) extends ParseException(
        s"[$thesaurus] Unsupported part of speech '$pos' for entry '$word'"
      )
