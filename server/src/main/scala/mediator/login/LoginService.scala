package mediator.login

import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Monad }
import derevo.derive
import derevo.tagless.applyK
import io.scalaland.chimney.dsl._
import mediator.db.user.get.Domain.Errors.GetError
import mediator.db.user.get.UserGetStorage
import mediator.login.Domain.Errors.LoginError
import mediator.login.Domain.Login
import mediator.token.TokenService
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.logging._
import tofu.syntax.raise._

@derive(applyK)
trait LoginService[F[_]] {
  def login(req: Login.Request): F[Either[LoginError, Login.Response]]
}

object LoginService extends Logging.Companion[LoginService] {
  final private class Impl[F[_]: Monad: LoginError.Errors](
      storage: UserGetStorage[F],
      tokenService: TokenService[F]
  ) extends LoginService[F] {
    override def login(req: Login.Request): F[Either[
      LoginError,
      Login.Response
    ]] =
      (for {
        user <-
          storage.getByEmail(req.email)
            .leftMapIn[LoginError] {
              case GetError.NotFound          => LoginError.EmailNotExist
              case GetError.PSQL(cause)       => LoginError.InternalDatabase(cause)
              case GetError.Connection(cause) => LoginError.Internal(cause)
            }
            .reRaise

        _ <- LoginError.WrongPassword.raise[F, Unit]
          .unlessA(user.hashedPassword.verifyHashedPassword(req.password))

        accessToken <- tokenService.createAccessToken(user.email)
      } yield accessToken.into[Login.Response].withFieldConst(
        _.accessToken,
        accessToken
      ).transform).attempt[LoginError]
  }

  final private class LogMid[F[_]: FlatMap: LoginService.Log]
    extends LoginService[Mid[F, *]] {
    override def login(req: Login.Request): Mid[
      F,
      Either[LoginError, Login.Response]
    ] =
      debug"Trying to login with email = ${req.email}" *>
        _.flatTap {
          case Left(error) =>
            error"Failed to login with email = ${req.email}. $error"
          case Right(_) => debug"Successfully login with email = ${req.email}"
        }
  }

  def make[F[_]: Monad: LoginError.Errors](
      storage: UserGetStorage[F],
      tokenService: TokenService[F]
  ): LoginService[F] = new Impl[F](storage, tokenService)

  def makeObservable[F[_]: Monad: LoginError.Errors: Logging.Make](
      storage: UserGetStorage[F],
      tokenService: TokenService[F]
  ): LoginService[F] = new LogMid[F] attach make[F](storage, tokenService)
}
