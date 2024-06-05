package mediator.db.user.update

import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, MonadThrow }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.Domain.User
import Domain.Errors.UpdateError
import Domain.UpdateUserRow
import mediator.db.user.UserStorage
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait UserUpdateStorage[F[_]] {
  def update(
      userId: User.ID,
      updateUserRow: UpdateUserRow
  ): F[Either[UpdateError, Unit]]
}

object UserUpdateStorage extends Logging.Companion[UserUpdateStorage] {
  final private class Impl[F[_]: MonadThrow](storage: UserStorage[F])
    extends UserUpdateStorage[F] {
    override def update(
        userId: User.ID,
        updateUserRow: UpdateUserRow
    ): F[Either[UpdateError, Unit]] =
      storage.update(userId, updateUserRow).map(Either.cond(_, (), UpdateError.NoUpdate))
  }

  final private class LogMid[F[_]: FlatMap: UserUpdateStorage.Log]
    extends UserUpdateStorage[Mid[F, *]] {
    override def update(
        userId: User.ID,
        updateUserRow: UpdateUserRow
    ): Mid[F, Either[UpdateError, Unit]] =
      debug"Trying update to row with id = $userId in users" *>
        _.flatTap {
          case Left(UpdateError.NoUpdate) =>
            warn"Row with id = $userId in users hasn't been updated"
          case Left(UpdateError.PSQL(cause)) =>
            errorCause"Failed to update row with id = $userId in users" (cause)
          case Left(UpdateError.Connection(cause)) =>
            errorCause"Failed to update row with id = $userId in users due to connection error" (
              cause
            )
          case Right(_) =>
            debug"Successfully updated row with id = $userId in users"
        }
  }

  private object Errors extends UserUpdateStorage[SQLErrorJoiner] {
    override def update(
        userId: User.ID,
        updateUserRow: UpdateUserRow
    ): SQLErrorJoiner[Either[UpdateError, Unit]] =
      SQLErrorJoiner[Either[UpdateError, Unit]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: UserUpdateStorage[F] =
    DatabaseRunner[UserUpdateStorage, F].wire(
      new Impl[ConnectionIO](UserStorage.db),
      Errors
    )

  def makeObservable[F[_]: MonadCancelThrow: SafeTransactor: Logging.Make]: UserUpdateStorage[F] = {
    val logMid = new LogMid[F]: UserUpdateStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
