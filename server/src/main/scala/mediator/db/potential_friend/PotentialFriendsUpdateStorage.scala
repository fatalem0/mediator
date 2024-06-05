package mediator.db.potential_friend

import cats.data.NonEmptyVector
import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Functor }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.Domain.User
import mediator.db.potential_friend.Domain.Errors.UpdateError
import mediator.db.user.get.Domain.UserWithMatchingPercent
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait PotentialFriendsUpdateStorage[F[_]] {
  def update(
      userID: User.ID,
      matchedUsers: NonEmptyVector[UserWithMatchingPercent]
  ): F[Either[UpdateError, Unit]]
}

object PotentialFriendsUpdateStorage
  extends Logging.Companion[PotentialFriendsUpdateStorage] {
  final private class Impl[F[_]: Functor](
      storage: PotentialFriendsStorage[F]
  ) extends PotentialFriendsUpdateStorage[F] {
    override def update(
        userID: User.ID,
        matchedUsers: NonEmptyVector[UserWithMatchingPercent]
    ): F[Either[UpdateError, Unit]] =
      storage.update(userID, matchedUsers).map(Either.cond(_, (), UpdateError.NoUpdate))
  }

  final private class LogMid[F[_]: FlatMap: PotentialFriendsUpdateStorage.Log]
    extends PotentialFriendsUpdateStorage[Mid[F, *]] {
    override def update(
        userID: User.ID,
        matchedUsers: NonEmptyVector[UserWithMatchingPercent]
    ): Mid[F, Either[UpdateError, Unit]] =
      debug"Trying to update row with user_id = $userID in potential_friends_users" *>
        _.flatTap {
          case Left(UpdateError.NoUpdate) =>
            warn"Row with user_id = $userID in potential_friends_users hasn't been updated"
          case Left(UpdateError.ReferenceNotFound(cause)) =>
            errorCause"""
                        Update of row with user_id = $userID in potential_friends_users
                        failed due to violation of foreign key"
                      """ (cause)
          case Left(UpdateError.PSQL(cause)) =>
            errorCause"Failed to update row with user_id = $userID in potential_friends_users" (
              cause
            )
          case Left(UpdateError.Connection(cause)) =>
            errorCause"Failed to update row with user_id = $userID in potential_friends_users due to connection error" (
              cause
            )
          case Right(_) =>
            debug"Successfully updated row with user_id = $userID in potential_friends_users"
        }
  }

  private object Errors extends PotentialFriendsUpdateStorage[SQLErrorJoiner] {
    override def update(
        userID: User.ID,
        matchedUsers: NonEmptyVector[UserWithMatchingPercent]
    ): SQLErrorJoiner[Either[UpdateError, Unit]] =
      SQLErrorJoiner[Either[UpdateError, Unit]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: PotentialFriendsUpdateStorage[F] =
    DatabaseRunner[PotentialFriendsUpdateStorage, F].wire(
      new Impl[ConnectionIO](
        PotentialFriendsStorage.db
      ),
      Errors
    )

  def makeObservable[
      F[_]: MonadCancelThrow: SafeTransactor: Logging.Make
  ]: PotentialFriendsUpdateStorage[F] = {
    val logMid = new LogMid[F]: PotentialFriendsUpdateStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
