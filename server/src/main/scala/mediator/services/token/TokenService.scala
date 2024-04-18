package mediator.services.token

import cats.Monad
import cats.effect.Clock
import cats.syntax.functor._
import cats.syntax.option._
import derevo.derive
import derevo.tagless.applyK
import pdi.jwt._
import tofu.generate.GenUUID
import mediator.Domain.{ AccessToken, UserEmail }

import scala.concurrent.duration.FiniteDuration

@derive(applyK)
trait TokenService[F[_]] {
  def createAccessToken(email: UserEmail): F[AccessToken]
}

object TokenService {
  private val key  = "secretKey"
  private val algo = JwtAlgorithm.HS256

  final private class Impl[F[_]: Monad: Clock: GenUUID](ttl: FiniteDuration)
    extends TokenService[F] {
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

  def make[F[_]: Monad: Clock: GenUUID](ttl: FiniteDuration): TokenService[F] =
    new Impl[F](ttl)
}
