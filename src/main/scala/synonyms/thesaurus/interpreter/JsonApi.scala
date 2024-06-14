package synonyms.thesaurus.interpreter

import cats.effect.Async
import fs2.*
import fs2.data.json.*
import fs2.data.json.circe.*
import fs2.data.json.codec.*
import org.http4s.*
import org.http4s.ember.client.*
import synonyms.thesaurus.algebra.Client
import synonyms.thesaurus.algebra.Client.ClientError
import cats.syntax.functor.*
import _root_.io.circe.*

trait JsonApi[Decodable: Decoder, F[_]: Async] extends Client[F]:
  override type Doc = Decodable
  def url(word: String): Uri

  def request(word: String): Request[F] = Request[F](Method.GET, url(word))

  override def fetchDocument(word: String): F[Either[ClientError, Doc]] =
    val words: Stream[F, Doc] = for
      client <- Stream.resource(EmberClientBuilder.default[F].build)
      res    <- client.stream(request(word))
      body <- res.body
        .through(text.utf8.decode)
        .through(tokens)
        .through(deserialize[F, Doc])
    yield body
    words.compile.toList.map(l => Right(l.head))
