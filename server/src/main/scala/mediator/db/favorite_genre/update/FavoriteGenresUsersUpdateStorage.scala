package mediator.db.favorite_genre.update

import cats.data.NonEmptyVector
import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Functor }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.Domain.{ Genre, User }
import Domain.Errors.UpdateError
import mediator.db.favorite_genre.FavoriteGenresUsersStorage
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait FavoriteGenresUsersUpdateStorage[F[_]] {
  def update(
      userId: User.ID,
      favoriteGenreIds: NonEmptyVector[Genre.ID]
  ): F[Either[UpdateError, Unit]]
}

object FavoriteGenresUsersUpdateStorage
  extends Logging.Companion[FavoriteGenresUsersUpdateStorage] {
  final private class Impl[F[_]: Functor](
      storage: FavoriteGenresUsersStorage[F]
  ) extends FavoriteGenresUsersUpdateStorage[F] {
    override def update(
        userId: User.ID,
        favoriteGenreIds: NonEmptyVector[Genre.ID]
    ): F[Either[UpdateError, Unit]] =
      storage.update(userId, favoriteGenreIds).map(Either.cond(_, (), UpdateError.NoUpdate))
  }

  final private class LogMid[F[_]: FlatMap: FavoriteGenresUsersUpdateStorage.Log]
    extends FavoriteGenresUsersUpdateStorage[Mid[F, *]] {
    override def update(
        userId: User.ID,
        favoriteGenreIds: NonEmptyVector[Genre.ID]
    ): Mid[F, Either[UpdateError, Unit]] =
      debug"Trying to update row with user_id = $userId in favorite_genres_users" *>
        _.flatTap {
          case Left(UpdateError.NoUpdate) =>
            warn"Row with user_id = $userId in favorite_genres_users hasn't been updated"
          case Left(UpdateError.ReferenceNotFound(cause)) =>
            errorCause"""
                        Update of row with user_id = $userId in favorite_genres_users
                        failed due to violation of foreign key"
                      """ (cause)
          case Left(UpdateError.PSQL(cause)) =>
            errorCause"Failed to update row with user_id = $userId in favorite_genres_users" (
              cause
            )
          case Left(UpdateError.Connection(cause)) =>
            errorCause"Failed to update row with user_id = $userId in favorite_genres_users due to connection error" (
              cause
            )
          case Right(_) =>
            debug"Successfully updated row with user_id = $userId in favorite_genres_users"
        }
  }

  private object Errors
    extends FavoriteGenresUsersUpdateStorage[SQLErrorJoiner] {
    override def update(
        userId: User.ID,
        favoriteGenreIds: NonEmptyVector[Genre.ID]
    ): SQLErrorJoiner[Either[UpdateError, Unit]] =
      SQLErrorJoiner[Either[UpdateError, Unit]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: FavoriteGenresUsersUpdateStorage[F] =
    DatabaseRunner[FavoriteGenresUsersUpdateStorage, F].wire(
      new Impl[ConnectionIO](
        FavoriteGenresUsersStorage.db
      ),
      Errors
    )

  def makeObservable[
      F[_]: MonadCancelThrow: SafeTransactor: Logging.Make
  ]: FavoriteGenresUsersUpdateStorage[F] = {
    val logMid = new LogMid[F]: FavoriteGenresUsersUpdateStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
