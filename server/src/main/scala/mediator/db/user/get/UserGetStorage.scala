package mediator.db.user.get

import cats.data.NonEmptyVector
import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Functor }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.Domain.{ Artist, Genre, Limit, User, UserPurpose }
import mediator.db.user.UserStorage
import mediator.db.user.get.Domain.Errors.GetError
import mediator.db.user.get.Domain.UserWithMatchingPercent
import mediator.potential_friend.Domain.MatchingPercent
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.foption._
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait UserGetStorage[F[_]] {
  def getByEmail(email: User.Email): F[Either[GetError, User]]
  def getByID(userID: User.ID): F[Either[GetError, User]]

  def getMatchedUsers(
      userID: User.ID,
      favoriteArtistIDs: NonEmptyVector[Artist.ID],
      favoriteGenreIDs: NonEmptyVector[Genre.ID],
      userPurposeIDs: NonEmptyVector[UserPurpose.ID],
      limit: Limit,
      matchingPercent: MatchingPercent
  ): F[Either[GetError, Vector[UserWithMatchingPercent]]]
}

object UserGetStorage extends Logging.Companion[UserGetStorage] {
  final private class Impl[F[_]: Functor](storage: UserStorage[F])
    extends UserGetStorage[F] {
    override def getByEmail(email: User.Email): F[Either[GetError, User]] =
      storage.getByEmail(email).toRightIn(GetError.notFound)

    override def getByID(userID: User.ID): F[Either[GetError, User]] =
      storage.getByID(userID).toRightIn(GetError.notFound)

    override def getMatchedUsers(
        userID: User.ID,
        favoriteArtistIDs: NonEmptyVector[Artist.ID],
        favoriteGenreIDs: NonEmptyVector[Genre.ID],
        userPurposeIDs: NonEmptyVector[UserPurpose.ID],
        limit: Limit,
        matchingPercent: MatchingPercent
    ): F[Either[GetError, Vector[UserWithMatchingPercent]]] =
      storage.getUserIDsWithSimilarInterests(
        userID,
        favoriteArtistIDs,
        favoriteGenreIDs,
        userPurposeIDs,
        limit,
        matchingPercent
      ).map(vector => Either.cond(vector.nonEmpty, vector, GetError.NotFound))
  }

  final private class LogMid[F[_]: FlatMap: UserGetStorage.Log]
    extends UserGetStorage[Mid[F, *]] {
    override def getByEmail(email: User.Email): Mid[F, Either[GetError, User]] =
      debug"Trying to get user by email = $email" *>
        _.flatTap {
          case Left(GetError.NotFound) =>
            warn"User with email = $email not found"
          case Left(GetError.PSQL(cause)) =>
            errorCause"Failed to get user by email" (cause)
          case Left(GetError.Connection(cause)) =>
            errorCause"Failed to get user by email due to connection error" (
              cause
            )
          case Right(_) => debug"Successfully got user by email = $email"
        }

    override def getByID(userID: User.ID): Mid[F, Either[GetError, User]] =
      debug"Trying to get user by id = $userID" *>
        _.flatTap {
          case Left(GetError.NotFound) => warn"User with id = $userID not found"
          case Left(GetError.PSQL(cause)) =>
            errorCause"Failed to get user by id" (cause)
          case Left(GetError.Connection(cause)) =>
            errorCause"Failed to get user by id due to connection error" (
              cause
            )
          case Right(_) => debug"Successfully got user by id = $userID"
        }

    override def getMatchedUsers(
        userID: User.ID,
        favoriteArtistIDs: NonEmptyVector[Artist.ID],
        favoriteGenreIDs: NonEmptyVector[Genre.ID],
        userPurposeIDs: NonEmptyVector[UserPurpose.ID],
        limit: Limit,
        matchingPercent: MatchingPercent
    ): Mid[F, Either[GetError, Vector[UserWithMatchingPercent]]] =
      debug"Trying to get user IDs with same interests as userID = $userID" *>
        _.flatTap {
          case Left(GetError.NotFound) =>
            warn"User IDs with the same interests as user with id = $userID not found"
          case Left(GetError.PSQL(cause)) =>
            errorCause"Failed to get user IDs with the same interests as user with id = $userID" (
              cause
            )
          case Left(GetError.Connection(cause)) =>
            errorCause"Failed to get user IDs with the same interests as user with id = $userID due to connection error" (
              cause
            )
          case Right(_) =>
            debug"Successfully got user IDs with the same interests as user with id = $userID"
        }
  }

  private object Errors extends UserGetStorage[SQLErrorJoiner] {
    override def getByEmail(email: User.Email): SQLErrorJoiner[Either[GetError, User]] =
      SQLErrorJoiner[Either[GetError, User]]

    override def getByID(userID: User.ID): SQLErrorJoiner[Either[GetError, User]] =
      SQLErrorJoiner[Either[GetError, User]]

    override def getMatchedUsers(
        userID: User.ID,
        favoriteArtistIDs: NonEmptyVector[Artist.ID],
        favoriteGenreIDs: NonEmptyVector[Genre.ID],
        userPurposeIDs: NonEmptyVector[UserPurpose.ID],
        limit: Limit,
        matchingPercent: MatchingPercent
    ): SQLErrorJoiner[Either[GetError, Vector[UserWithMatchingPercent]]] =
      SQLErrorJoiner[Either[GetError, Vector[UserWithMatchingPercent]]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: UserGetStorage[F] =
    DatabaseRunner[UserGetStorage, F].wire(
      new Impl[ConnectionIO](
        UserStorage.db
      ),
      Errors
    )

  def makeObservable[F[_]: MonadCancelThrow: SafeTransactor: Logging.Make]: UserGetStorage[F] = {
    val logMid = new LogMid[F]: UserGetStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
