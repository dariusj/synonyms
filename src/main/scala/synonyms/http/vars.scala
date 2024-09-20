package synonyms.http

import synonyms.domain.Word

object WordVar:
  def unapply(str: String): Option[Word] = Word.option(str)
