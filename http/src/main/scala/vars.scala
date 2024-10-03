package synonyms.http

import synonyms.core.domain.Word

object WordVar:
  def unapply(str: String): Option[Word] = Word.option(str)
