package mediator.db.favorite_artist.update

import cats.data.NonEmptyVector
import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Functor }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.Domain.{ Artist, User }
import mediator.db.favorite_artist.FavoriteArtistsUsersStorage
import mediator.db.favorite_artist.update.Domain.Errors.UpdateError
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait FavoriteArtistsUsersUpdateStorage[F[_]] {
  def update(
      userId: User.ID,
      favoriteArtistIds: NonEmptyVector[Artist.ID]
  ): F[Either[UpdateError, Unit]]
}

object FavoriteArtistsUsersUpdateStorage
  extends Logging.Companion[FavoriteArtistsUsersUpdateStorage] {
  final private class Impl[F[_]: Functor](
      storage: FavoriteArtistsUsersStorage[F]
  ) extends FavoriteArtistsUsersUpdateStorage[F] {
    override def update(
        userId: User.ID,
        favoriteArtistIds: NonEmptyVector[Artist.ID]
    ): F[Either[UpdateError, Unit]] =
      storage.update(userId, favoriteArtistIds).map(Either.cond(_, (), UpdateError.NoUpdate))
  }

  final private class LogMid[F[_]: FlatMap: FavoriteArtistsUsersUpdateStorage.Log]
    extends FavoriteArtistsUsersUpdateStorage[Mid[F, *]] {
    override def update(
        userId: User.ID,
        favoriteArtistIds: NonEmptyVector[Artist.ID]
    ): Mid[F, Either[UpdateError, Unit]] =
      debug"Trying to update row with user_id = $userId in favorite_artists_users" *>
        _.flatTap {
          case Left(UpdateError.NoUpdate) =>
            warn"Row with user_id = $userId in favorite_artists_users hasn't been updated"
          case Left(UpdateError.ReferenceNotFound(cause)) =>
            errorCause"""
                        Update of row with user_id = $userId in favorite_artists_users
                        failed due to violation of foreign key"
                      """ (cause)
          case Left(UpdateError.PSQL(cause)) =>
            errorCause"Failed to update row with user_id = $userId in favorite_artists_users" (
              cause
            )
          case Left(UpdateError.Connection(cause)) =>
            errorCause"Failed to update row with user_id = $userId in favorite_artists_users due to connection error" (
              cause
            )
          case Right(_) =>
            debug"Successfully updated row with user_id = $userId in favorite_artists_users"
        }
  }

  private object Errors
    extends FavoriteArtistsUsersUpdateStorage[SQLErrorJoiner] {
    override def update(
        userId: User.ID,
        favoriteArtistIds: NonEmptyVector[Artist.ID]
    ): SQLErrorJoiner[Either[UpdateError, Unit]] =
      SQLErrorJoiner[Either[UpdateError, Unit]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: FavoriteArtistsUsersUpdateStorage[F] =
    DatabaseRunner[FavoriteArtistsUsersUpdateStorage, F].wire(
      new Impl[ConnectionIO](
        FavoriteArtistsUsersStorage.db
      ),
      Errors
    )

  def makeObservable[
      F[_]: MonadCancelThrow: SafeTransactor: Logging.Make
  ]: FavoriteArtistsUsersUpdateStorage[F] = {
    val logMid = new LogMid[F]: FavoriteArtistsUsersUpdateStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
