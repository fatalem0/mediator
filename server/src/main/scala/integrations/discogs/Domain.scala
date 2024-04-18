package integrations.discogs

import cats.syntax.either._
import derevo.derive
import derevo.tethys.tethysReader
import enumeratum.values.{StringEnum, StringEnumEntry}
import integrations.discogs.Domain.RequestToken.CallbackConfirmed
import io.estatico.newtype.macros.newtype
import sttp.client3._
import sttp.model.StatusCode
import tethys._
import tethys.enumeratum.StringTethysEnum
import tethys.readers.tokens.TokenIteratorProducer

object Domain {
  object Errors {
    sealed trait DiscogsTokenClientError

    object DiscogsTokenClientError {
      final case class Connect(error: SttpClientException) extends DiscogsTokenClientError
      final case class Unauthorized(msg: String) extends DiscogsTokenClientError
      final case class Forbidden(msg: String) extends DiscogsTokenClientError
      final case class NotFound(msg: String) extends DiscogsTokenClientError
      final case class MethodNotAllowed(msg: String) extends DiscogsTokenClientError
      final case class UnprocessableEntity(msg: String) extends DiscogsTokenClientError
      final case class Client(msg: String) extends DiscogsTokenClientError
      final case class Server(msg: String) extends DiscogsTokenClientError
      final case class Unexpected(msg: String) extends DiscogsTokenClientError
      final case class Deserialization(cause: Throwable) extends DiscogsTokenClientError

      def responseAs[Resp: JsonReader](implicit
          tokenIteratorProducer: TokenIteratorProducer
      ): ResponseAs[Either[DiscogsTokenClientError, Resp], Any] =
        asStringAlways.mapWithMetadata {
          case (b, m) if m.code == StatusCode.Ok =>
            b.jsonAs[Resp]
              .fold(
                error => Deserialization(error).asLeft[Resp],
                resp  => resp.asRight[Deserialization]
              )

          case (b, m) if m.code == StatusCode.Unauthorized =>
            Unauthorized(b).asLeft[Resp]

          case (b, m) if m.code == StatusCode.Forbidden =>
            Forbidden(b).asLeft[Resp]

          case (b, m) if m.code == StatusCode.NotFound =>
            NotFound(b).asLeft[Resp]

          case (b, m) if m.code == StatusCode.MethodNotAllowed =>
            MethodNotAllowed(b).asLeft[Resp]

          case (b, m) if m.code == StatusCode.UnprocessableEntity =>
            UnprocessableEntity(b).asLeft[Resp]

          case (b, m) if m.isClientError =>
            Client(b).asLeft[Resp]

          case (b, m) if m.isServerError =>
            Server(b).asLeft[Resp]

          case (b, _) =>
            Unexpected(b).asLeft[Resp]
        }
    }
  }

  @newtype final case class TokenValue(value: String)
  @newtype final case class TokenSecret(value: String)

  @derive(tethysReader)
  final case class OauthToken(
      tokenValue: TokenValue,
      tokenSecret: TokenSecret
  )
}
