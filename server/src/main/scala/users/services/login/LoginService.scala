package users.services.login

import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Monad }
import derevo.derive
import derevo.tagless.applyK
import io.scalaland.chimney.dsl._
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.logging._
import tofu.syntax.raise._
import users.db.users.Domain.Errors.ReadError
import users.db.users.UserStorage
import users.services.login.Domain.Errors.LoginError
import users.services.login.Domain.Login
import users.services.token.TokenService

@derive(applyK)
trait LoginService[F[_]] {
  def login(req: Login.Request): F[Either[LoginError, Login.Response]]
}

object LoginService extends Logging.Companion[LoginService] {
  final private class Impl[F[_]: Monad: LoginError.Errors](
      userStorage: UserStorage[F],
      tokenService: TokenService[F]
  ) extends LoginService[F] {
    override def login(req: Login.Request)
        : F[Either[LoginError, Login.Response]] =
      (for {
        user <-
          userStorage.findByEmail(req.email)
            .leftMapIn[LoginError] {
              case ReadError.NotFound =>
                LoginError.EmailNotExist
              case ReadError.PSQL(cause) =>
                LoginError.InternalDatabase(cause)
              case ReadError.Connection(cause) =>
                LoginError.Internal(cause)
            }
            .reRaise

        _ <-
          LoginError.WrongPassword.raise[F, Unit]
            .unlessA(user.hashedPassword.verifyHashedPassword(req.password))

        accessToken <- tokenService.createAccessToken(user.email)
      } yield accessToken.into[Login.Response].withFieldConst(
        _.accessToken,
        accessToken
      ).transform).attempt[LoginError]
  }

  final private class LogMid[F[_]: FlatMap: LoginService.Log]
    extends LoginService[Mid[F, *]] {
    override def login(req: Login.Request)
        : Mid[F, Either[LoginError, Login.Response]] =
      debug"Trying to login with email = ${req.email}" *>
        _.flatTap {
          case Left(error) =>
            error"Failed to login with email = ${req.email}. $error"
          case Right(_) => debug"Successfully login with email = ${req.email}"
        }
  }

  def make[F[_]: Monad: LoginError.Errors](
      userStorage: UserStorage[F],
      tokenService: TokenService[F]
  ): LoginService[F] =
    new Impl[F](userStorage, tokenService)

  def makeObservable[F[_]: Monad: LoginError.Errors: Logging.Make](
      userStorage: UserStorage[F],
      tokenService: TokenService[F]
  ): LoginService[F] =
    new LogMid[F] attach make[F](userStorage, tokenService)
}
