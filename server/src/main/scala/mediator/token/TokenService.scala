package mediator.token

import cats.Monad
import cats.effect.Clock
import cats.syntax.functor._
import cats.syntax.option._
import derevo.derive
import derevo.tagless.applyK
import mediator.Domain.User
import mediator.Domain.User.AccessToken
import pdi.jwt._
import tofu.generate.GenUUID

import scala.concurrent.duration.FiniteDuration

@derive(applyK)
trait TokenService[F[_]] {
  def createAccessToken(email: User.Email): F[AccessToken]
}

object TokenService {
  private val key  = "secretKey"
  private val algo = JwtAlgorithm.HS256

  final private class Impl[F[_]: Monad: Clock: GenUUID](ttl: FiniteDuration)
    extends TokenService[F] {
    override def createAccessToken(email: User.Email): F[AccessToken] =
      for {
        now <- Clock[F].realTime
        claim = createJwtClaim(email, now)
        token = Jwt.encode(claim, key, algo)
      } yield AccessToken(token)

    private def createJwtClaim(
        email: User.Email,
        now: FiniteDuration
    ): JwtClaim = JwtClaim(
      content = email.value,
      expiration = now.plus(ttl).toSeconds.some,
      issuedAt = now.toSeconds.some
    )
  }

  def make[F[_]: Monad: Clock: GenUUID](ttl: FiniteDuration): TokenService[F] =
    new Impl[F](ttl)
}
