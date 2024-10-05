package synonyms.http

import cats.data.NonEmptyChain
import cats.laws.discipline.arbitrary.*
import org.scalacheck.{Arbitrary, Gen}
import synonyms.http.config.types.ConfigError

object PropHelpers:
  val configErrorGen: Gen[ConfigError] = for {
    nec <- Arbitrary.arbitrary[NonEmptyChain[String]]
  } yield ConfigError(nec)
