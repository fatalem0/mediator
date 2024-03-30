package users.services.auth

import cats.Monad
import cats.effect.Clock
import cats.syntax.functor._
import cats.syntax.option._
import derevo.derive
import derevo.tagless.applyK
import pdi.jwt._
import tofu.generate.GenUUID
import users.Domain.{ AccessToken, UserEmail }

import scala.concurrent.duration.FiniteDuration

@derive(applyK)
trait AuthService[F[_]] {
  def createAccessToken(email: UserEmail): F[AccessToken]
}

object AuthService {
  private val key  = "secretKey"
  private val algo = JwtAlgorithm.HS256

  final private class Impl[F[_]: Monad: Clock: GenUUID](ttl: FiniteDuration)
    extends AuthService[F] {
    override def createAccessToken(email: UserEmail): F[AccessToken] =
      for {
        now <- Clock[F].realTime
        claim = createJwtClaim(email, now)
        token = Jwt.encode(claim, key, algo)
      } yield AccessToken(token)

    private def createJwtClaim(
        email: UserEmail,
        now: FiniteDuration
    ): JwtClaim =
      JwtClaim(
        content = email.value,
        expiration = now.plus(ttl).toSeconds.some,
        issuedAt = now.toSeconds.some
      )
  }

  def make[F[_]: Monad: Clock: GenUUID](ttl: FiniteDuration): AuthService[F] =
    new Impl[F](ttl)
}
