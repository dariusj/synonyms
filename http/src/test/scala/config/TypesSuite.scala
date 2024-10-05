package synonyms.http.config

import cats.Eq
import cats.kernel.laws.discipline.SemigroupTests
import munit.DisciplineSuite
import org.scalacheck.Arbitrary
import synonyms.http.PropHelpers.configErrorGen
import synonyms.http.config.types.ConfigError

class TypesSuite extends DisciplineSuite:
  given Arbitrary[ConfigError] = Arbitrary(configErrorGen)
  given Eq[ConfigError]        = Eq.by(_.errors)
  checkAll("Semigroup[ConfigError]", SemigroupTests[ConfigError].semigroup)
