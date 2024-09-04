package synonyms.thesaurus.interpreter

import _root_.io.circe.*
import cats.effect.Async
import cats.syntax.functor.*
import fs2.*
import fs2.data.json.*
import fs2.data.json.circe.*
import fs2.data.json.codec.*
import fs2.io.net.Network
import org.http4s.*
import org.http4s.ember.client.*
import synonyms.thesaurus.*
import synonyms.thesaurus.algebra.Client

trait JsonApi[Decodable: Decoder, F[_]: Async: Network] extends Client[F]:
  override type Doc = Decodable
  def url(word: Word): Uri

  def request(word: Word): Request[F] = Request[F](Method.GET, url(word))

  override def fetchDocument(word: Word): F[Option[Doc]] =
    val words: Stream[F, Doc] = for
      client <- Stream.resource(EmberClientBuilder.default[F].build)
      res    <- client.stream(request(word))
      body <- res.body
        .through(text.utf8.decode)
        .through(tokens)
        .through(deserialize[F, Doc])
    yield body
    // TODO: Error handling
    words.compile.toList.map(l => Option(l.head))
