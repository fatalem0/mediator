package mediator.db.user.check

import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Functor }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.Domain.User
import mediator.db.user.UserStorage
import mediator.db.user.check.Domain.Errors.CheckError
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait UserCheckStorage[F[_]] {
  def checkIfExistsByEmail(email: User.Email): F[Either[CheckError, Boolean]]
}

object UserCheckStorage extends Logging.Companion[UserCheckStorage] {
  final private class Impl[F[_]: Functor](storage: UserStorage[F])
    extends UserCheckStorage[F] {
    override def checkIfExistsByEmail(email: User.Email): F[Either[CheckError, Boolean]] =
      storage.getByEmail(email).map(_.isDefined).rightIn[CheckError]
  }

  final private class LogMid[F[_]: FlatMap: UserCheckStorage.Log]
    extends UserCheckStorage[Mid[F, *]] {
    override def checkIfExistsByEmail(email: User.Email): Mid[F, Either[CheckError, Boolean]] =
      debug"Checking if user with email = $email exists" *>
        _.flatTap {
          case Left(CheckError.PSQL(cause)) =>
            errorCause"Failed to check is user with email = $email exists" (
              cause
            )
          case Left(CheckError.Connection(cause)) =>
            errorCause"Failed to check is user with email = $email exists due to connection error" (
              cause
            )
          case Right(true)  => debug"Successfully find user with email = $email"
          case Right(false) => debug"Cannot find user with email = $email"
        }
  }

  private object Errors extends UserCheckStorage[SQLErrorJoiner] {
    override def checkIfExistsByEmail(email: User.Email): SQLErrorJoiner[Either[
      CheckError,
      Boolean
    ]] =
      SQLErrorJoiner[Either[CheckError, Boolean]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: UserCheckStorage[F] =
    DatabaseRunner[UserCheckStorage, F].wire(
      new Impl[ConnectionIO](
        UserStorage.db
      ),
      Errors
    )

  def makeObservable[F[_]: MonadCancelThrow: SafeTransactor: Logging.Make]: UserCheckStorage[F] = {
    val logMid = new LogMid[F]: UserCheckStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
