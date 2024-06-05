package mediator.db.favorite_genre.get

import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Functor }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.Domain.{ Genre, User }
import mediator.db.favorite_genre.FavoriteGenresUsersStorage
import mediator.db.favorite_genre.get.Domain.Errors.GetError
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait FavoriteGenresUsersGetStorage[F[_]] {
  def get(userID: User.ID): F[Either[GetError, Vector[Genre]]]
}

object FavoriteGenresUsersGetStorage
  extends Logging.Companion[FavoriteGenresUsersGetStorage] {
  final private class Impl[F[_]: Functor](
      storage: FavoriteGenresUsersStorage[F]
  ) extends FavoriteGenresUsersGetStorage[F] {
    override def get(userID: User.ID): F[Either[GetError, Vector[Genre]]] =
      storage.getUserFavoriteGenres(userID).map(vector =>
        Either.cond(vector.nonEmpty, vector, GetError.NotFound)
      )
  }

  final private class LogMid[F[_]: FlatMap: FavoriteGenresUsersGetStorage.Log]
    extends FavoriteGenresUsersGetStorage[Mid[F, *]] {
    override def get(userID: User.ID): Mid[F, Either[GetError, Vector[Genre]]] =
      debug"Trying to get user's favorite genres" *>
        _.flatTap {
          case Left(GetError.NotFound) =>
            warn"User's favorite genres are not found"
          case Left(GetError.PSQL(cause)) =>
            errorCause"Failed to get user's favorite genres" (cause)
          case Left(GetError.Connection(cause)) =>
            errorCause"Failed to get user's favorite genres" (cause)
          case Right(_) => debug"Successfully got user's favorite genres"
        }
  }

  private object Errors extends FavoriteGenresUsersGetStorage[SQLErrorJoiner] {
    override def get(userID: User.ID): SQLErrorJoiner[Either[GetError, Vector[Genre]]] =
      SQLErrorJoiner[Either[GetError, Vector[Genre]]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: FavoriteGenresUsersGetStorage[F] =
    DatabaseRunner[FavoriteGenresUsersGetStorage, F].wire(
      new Impl[ConnectionIO](
        FavoriteGenresUsersStorage.db
      ),
      Errors
    )

  def makeObservable[
      F[_]: MonadCancelThrow: SafeTransactor: Logging.Make
  ]: FavoriteGenresUsersGetStorage[F] = {
    val logMid = new LogMid[F]: FavoriteGenresUsersGetStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
