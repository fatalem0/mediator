package mediator.registration

import cats.effect.Clock
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Monad }
import derevo.derive
import derevo.tagless.applyK
import mediator.registration.Domain.Errors.RegistrationError
import mediator.registration.Domain.Registration
import mediator.token.TokenService
import mediator.user.create.UserCreateService
import tofu.generate.GenUUID
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.logging._

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
      userService: UserCreateService[F]
  ) extends RegistrationService[F] {
    override def register(
        req: Registration.Request
    ): F[Either[RegistrationError, Registration.Response]] =
      (for {
        userId <-
          userService
            .create(req)
            .leftMapIn[RegistrationError](RegistrationError.fromUserError)
            .reRaise

        accessToken <- tokenService.createAccessToken(req.email)
      } yield Registration.Response(userId, accessToken)).attempt[RegistrationError]
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
      userService: UserCreateService[F]
  ): RegistrationService[F] = new Impl[F](tokenService, userService)

  def makeObservable[F[_]: Monad: Clock: GenUUID: RegistrationError.Errors: Logging.Make](
      tokenService: TokenService[F],
      userService: UserCreateService[F]
  ): RegistrationService[F] =
    new LogMid[F] attach make[F](tokenService, userService)
}
