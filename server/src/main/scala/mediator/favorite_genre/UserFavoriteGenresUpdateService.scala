package mediator.favorite_genre

import cats.data.NonEmptyVector
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Monad }
import derevo.derive
import derevo.tagless.applyK
import mediator.Domain.User
import mediator.db.favorite_genre.update.Domain.Errors.UpdateError
import mediator.db.favorite_genre.update.FavoriteGenresUsersUpdateStorage
import mediator.favorite_genre.Domain.Errors.UpdateUserFavoriteGenresError
import mediator.favorite_genre.Domain.Errors.UpdateUserFavoriteGenresError.NoFavoriteGenres
import mediator.favorite_genre.Domain.UpdateUserFavoriteGenres
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.logging._
import tofu.syntax.raise._

@derive(applyK)
trait UserFavoriteGenresUpdateService[F[_]] {
  def update(
      userId: User.ID,
      req: UpdateUserFavoriteGenres.Request
  ): F[Either[UpdateUserFavoriteGenresError, Unit]]
}

object UserFavoriteGenresUpdateService
  extends Logging.Companion[UserFavoriteGenresUpdateService] {
  final private class Impl[F[_]: Monad: UpdateUserFavoriteGenresError.Errors](
      storage: FavoriteGenresUsersUpdateStorage[F]
  ) extends UserFavoriteGenresUpdateService[F] {
    override def update(
        userId: User.ID,
        req: UpdateUserFavoriteGenres.Request
    ): F[Either[UpdateUserFavoriteGenresError, Unit]] =
      (for {
        _ <- NoFavoriteGenres.raise[F, Unit].whenA(req.favoriteGenreIds.isEmpty)

        favoriteGenreIds = NonEmptyVector.fromVectorUnsafe(req.favoriteGenreIds)

        _ <-
          storage.update(userId, favoriteGenreIds)
            .leftMapIn[UpdateUserFavoriteGenresError] {
              case UpdateError.ReferenceNotFound(_) | UpdateError.NoUpdate =>
                UpdateUserFavoriteGenresError.UserFavoriteGenresNotUpdated
              case UpdateError.PSQL(cause) =>
                UpdateUserFavoriteGenresError.InternalDatabase(cause)
              case UpdateError.Connection(cause) =>
                UpdateUserFavoriteGenresError.Internal(cause)
            }
            .reRaise
      } yield ()).attempt[UpdateUserFavoriteGenresError]
  }

  final private class LogMid[F[_]: FlatMap: UserFavoriteGenresUpdateService.Log]
    extends UserFavoriteGenresUpdateService[Mid[F, *]] {
    override def update(
        userId: User.ID,
        req: UpdateUserFavoriteGenres.Request
    ): Mid[F, Either[UpdateUserFavoriteGenresError, Unit]] =
      debug"Updating list of favorite genres for user with id = $userId" *>
        _.flatTap {
          case Left(error) =>
            error"Failed to update list of favorite genres for user with id = $userId. $error"
          case Right(_) =>
            debug"Successfully updated list of favorite genres for user with id = $userId"
        }
  }

  def make[F[_]: Monad: UpdateUserFavoriteGenresError.Errors](
      storage: FavoriteGenresUsersUpdateStorage[F]
  ): UserFavoriteGenresUpdateService[F] = new Impl[F](storage)

  def makeObservable[
      F[_]: Monad: UpdateUserFavoriteGenresError.Errors: UserFavoriteGenresUpdateService.Log
  ](
      storage: FavoriteGenresUsersUpdateStorage[F]
  ): UserFavoriteGenresUpdateService[F] = new LogMid[F] attach make[F](storage)
}
