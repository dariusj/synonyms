package synonyms.http

import synonyms.domain.Word

object WordVar:
  def unapply(str: String): Option[Word] = Option.when(str.nonEmpty)(Word(str))
