package synonyms.thesaurus

import cats.effect.IO
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.interpreter.*

trait ClientProvider[F[_]]:
  def cambridge: Client[F]
  def collins: Client[F]
  def datamuse: Client[F]
  def mw: Client[F]

object ClientProvider:
  given ioProvider: ClientProvider[IO] with
    val cambridge: Client[IO] = Cambridge[IO]
    val collins: Client[IO]   = Collins[IO]
    val datamuse: Client[IO]  = Datamuse[IO]
    val mw: Client[IO]        = MerriamWebster[IO]
