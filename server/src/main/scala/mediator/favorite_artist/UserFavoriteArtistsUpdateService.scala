package mediator.favorite_artist

import cats.data.NonEmptyVector
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Monad }
import derevo.derive
import derevo.tagless.applyK
import mediator.Domain.User
import mediator.db.favorite_artist.update.Domain.Errors.UpdateError
import mediator.db.favorite_artist.update.FavoriteArtistsUsersUpdateStorage
import mediator.favorite_artist.Domain.Errors.UpdateUserFavoriteArtistsError
import mediator.favorite_artist.Domain.Errors.UpdateUserFavoriteArtistsError.SmallNumberOfFavoriteArtists
import mediator.favorite_artist.Domain.UpdateUserFavoriteArtists.Request
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.logging._
import tofu.syntax.raise._

@derive(applyK)
trait UserFavoriteArtistsUpdateService[F[_]] {
  def update(
      userId: User.ID,
      req: Request
  ): F[Either[UpdateUserFavoriteArtistsError, Unit]]
}

object UserFavoriteArtistsUpdateService
  extends Logging.Companion[UserFavoriteArtistsUpdateService] {
  final private class Impl[F[_]: Monad: UpdateUserFavoriteArtistsError.Errors](
      storage: FavoriteArtistsUsersUpdateStorage[F]
  ) extends UserFavoriteArtistsUpdateService[F] {
    override def update(
        userId: User.ID,
        req: Request
    ): F[Either[UpdateUserFavoriteArtistsError, Unit]] =
      (for {
        _ <-
          SmallNumberOfFavoriteArtists
            .raise[F, Unit]
            .whenA(req.favoriteArtistIds.length < 3)

        favoriteArtistIds =
          NonEmptyVector.fromVectorUnsafe(
            req.favoriteArtistIds
          )

        _ <-
          storage.update(userId, favoriteArtistIds)
            .leftMapIn[UpdateUserFavoriteArtistsError] {
              case UpdateError.ReferenceNotFound(_) | UpdateError.NoUpdate =>
                UpdateUserFavoriteArtistsError.UserFavoriteArtistsNotUpdated
              case UpdateError.PSQL(cause) =>
                UpdateUserFavoriteArtistsError.InternalDatabase(cause)
              case UpdateError.Connection(cause) =>
                UpdateUserFavoriteArtistsError.Internal(cause)
            }
            .reRaise
      } yield ()).attempt[UpdateUserFavoriteArtistsError]
  }

  final private class LogMid[
      F[_]: FlatMap: UserFavoriteArtistsUpdateService.Log
  ] extends UserFavoriteArtistsUpdateService[Mid[F, *]] {
    override def update(
        userId: User.ID,
        req: Request
    ): Mid[F, Either[UpdateUserFavoriteArtistsError, Unit]] =
      debug"Updating list of favorite artists for user with id = $userId" *>
        _.flatTap {
          case Left(error) =>
            error"Failed to update list of favorite artists for user with id = $userId. $error"
          case Right(_) =>
            debug"Successfully updated list of favorite artists for user with id = $userId"
        }
  }

  def make[F[_]: Monad: UpdateUserFavoriteArtistsError.Errors](
      storage: FavoriteArtistsUsersUpdateStorage[F]
  ): UserFavoriteArtistsUpdateService[F] = new Impl[F](storage)

  def makeObservable[
      F[_]: Monad: UpdateUserFavoriteArtistsError.Errors: UserFavoriteArtistsUpdateService.Log
  ](
      storage: FavoriteArtistsUsersUpdateStorage[F]
  ): UserFavoriteArtistsUpdateService[F] = new LogMid[F] attach make[F](storage)
}
