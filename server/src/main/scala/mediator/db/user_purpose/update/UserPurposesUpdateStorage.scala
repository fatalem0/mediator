package mediator.db.user_purpose.update

import cats.data.NonEmptyVector
import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Functor }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.Domain.{ User, UserPurpose }
import mediator.db.user_purpose.UserPurposesStorage
import mediator.db.user_purpose.update.Domain.Errors.UpdateError
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait UserPurposesUpdateStorage[F[_]] {
  def update(
      userId: User.ID,
      userPurposeIds: NonEmptyVector[UserPurpose.ID]
  ): F[Either[UpdateError, Unit]]
}

object UserPurposesUpdateStorage
  extends Logging.Companion[UserPurposesUpdateStorage] {
  final private class Impl[F[_]: Functor](
      storage: UserPurposesStorage[F]
  ) extends UserPurposesUpdateStorage[F] {
    override def update(
        userId: User.ID,
        userPurposeIds: NonEmptyVector[UserPurpose.ID]
    ): F[Either[UpdateError, Unit]] =
      storage.update(userId, userPurposeIds).map(Either.cond(_, (), UpdateError.NoUpdate))
  }

  final private class LogMid[F[_]: FlatMap: UserPurposesUpdateStorage.Log]
    extends UserPurposesUpdateStorage[Mid[F, *]] {
    override def update(
        userId: User.ID,
        userPurposeIds: NonEmptyVector[UserPurpose.ID]
    ): Mid[F, Either[UpdateError, Unit]] =
      debug"Trying to update row with user_id = $userId in user_purposes_users" *>
        _.flatTap {
          case Left(UpdateError.NoUpdate) =>
            warn"Row with user_id = $userId in user_purposes_users hasn't been updated"
          case Left(UpdateError.ReferenceNotFound(cause)) =>
            errorCause"""
                        Update of row with user_id = $userId in user_purposes_users
                        failed due to violation of foreign key"
                      """ (cause)
          case Left(UpdateError.PSQL(cause)) =>
            errorCause"Failed to update row with user_id = $userId in user_purposes_users" (
              cause
            )
          case Left(UpdateError.Connection(cause)) =>
            errorCause"Failed to update row with user_id = $userId in user_purposes_users due to connection error" (
              cause
            )
          case Right(_) =>
            debug"Successfully updated row with user_id = $userId in user_purposes_users"
        }
  }

  private object Errors extends UserPurposesUpdateStorage[SQLErrorJoiner] {
    override def update(
        userId: User.ID,
        userPurposeIds: NonEmptyVector[UserPurpose.ID]
    ): SQLErrorJoiner[Either[UpdateError, Unit]] =
      SQLErrorJoiner[Either[UpdateError, Unit]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: UserPurposesUpdateStorage[F] =
    DatabaseRunner[UserPurposesUpdateStorage, F].wire(
      new Impl[ConnectionIO](UserPurposesStorage.db),
      Errors
    )

  def makeObservable[
      F[_]: MonadCancelThrow: SafeTransactor: Logging.Make
  ]: UserPurposesUpdateStorage[F] = {
    val logMid = new LogMid[F]: UserPurposesUpdateStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
