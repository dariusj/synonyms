package synonyms.http.config

import cats.kernel.laws.discipline.SemigroupTests
import munit.DisciplineSuite
import synonyms.http.PropHelpers.given
import synonyms.http.config.types.ConfigError

class TypesSuite extends DisciplineSuite:
  checkAll("Semigroup[ConfigError]", SemigroupTests[ConfigError].semigroup)
