package users.services.registration

import cats.effect.Clock
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Monad }
import derevo.derive
import derevo.tagless.applyK
import io.scalaland.chimney.dsl._
import tofu.generate.GenUUID
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.logging._
import tofu.syntax.raise._
import users.services.registration.Domain.Errors.RegistrationError
import users.services.registration.Domain.Registration
import users.services.token.TokenService
import users.services.users.UserService

@derive(applyK)
trait RegistrationService[F[_]] {
  def register(
      req: Registration.Request
  ): F[Either[RegistrationError, Registration.Response]]
}

object RegistrationService extends Logging.Companion[RegistrationService] {
  final private class Impl[F[
      _
  ]: Monad: Clock: GenUUID: RegistrationError.Errors](
      tokenService: TokenService[F],
      userService: UserService[F]
  ) extends RegistrationService[F] {
    override def register(
        req: Registration.Request
    ): F[Either[RegistrationError, Registration.Response]] =
      (for {
        _ <-
          userService
            .create(req)
            .leftMapF[RegistrationError](
              RegistrationError.fromUserError(_).raise[F, RegistrationError]
            )

        accessToken <- tokenService.createAccessToken(req.email)
      } yield accessToken
        .into[Registration.Response]
        .withFieldConst(_.accessToken, accessToken)
        .transform).attempt[RegistrationError]
  }

  final private class LogMid[F[_]: FlatMap: RegistrationService.Log]
    extends RegistrationService[Mid[F, *]] {
    override def register(
        req: Registration.Request
    ): Mid[F, Either[RegistrationError, Registration.Response]] =
      debug"Trying to register new user with email = ${req.email}" *>
        _.flatTap {
          case Left(error) => error"Failed to register new user. $error"
          case Right(_) =>
            debug"Successfully registered new user with email = ${req.email}"
        }
  }

  def make[F[_]: Monad: Clock: GenUUID: RegistrationError.Errors](
      tokenService: TokenService[F],
      userService: UserService[F]
  ): RegistrationService[F] =
    new Impl[F](tokenService, userService)

  def makeObservable[F[
      _
  ]: Monad: Clock: GenUUID: RegistrationError.Errors: Logging.Make](
      tokenService: TokenService[F],
      userService: UserService[F]
  ): RegistrationService[F] =
    new LogMid[F] attach make[F](tokenService, userService)
}
