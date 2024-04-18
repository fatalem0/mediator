package mediator.services.discogs

import cats.FlatMap
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.flatMap._
import integrations.discogs.DiscogsTokenClient
import integrations.discogs.Domain.{OauthToken, Token}
import mediator.services.discogs.Domain.Discogs
import mediator.services.discogs.Domain.Errors.DiscogsTokenError
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._

trait DiscogsTokenService[F[_]] {
  def getRequestToken: F[Either[DiscogsTokenError, OauthToken]]
  def getAccessToken(oauthToken: Token): F[Either[DiscogsTokenError, OauthToken]]
}

object DiscogsTokenService extends Logging.Companion[DiscogsTokenService] {
  final private class Impl[F[_]](client: DiscogsTokenClient[F]) extends DiscogsTokenService[F] {
    override def getRequestToken: F[Either[DiscogsTokenError, OauthToken]] =
      client.getRequestToken

    override def getAccessToken(oauthToken: Token): F[Either[DiscogsTokenError, OauthToken]] =
      client.getAccessToken(oauthToken)
  }

  final private class LogMid[F[_]: FlatMap: DiscogsTokenService.Log] extends DiscogsTokenService[Mid[F, *]] {
    override def getRequestToken: Mid[F, Either[DiscogsTokenError, RequestToken]] =
      debug"Calling Discogs' /request_token" *>
        _.flatTap {
          case Left(error) =>
            error"Failed with error: $error"
          case Right(_) =>
            debug"Successfully requested Discogs' /request_token"
        }

    override def getAccessToken(oauthToken: Token): Mid[F, Either[DiscogsTokenError, AccessToken]] = ???
  }
}
