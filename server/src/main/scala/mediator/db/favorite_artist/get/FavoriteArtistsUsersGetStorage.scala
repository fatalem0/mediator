package mediator.db.favorite_artist.get

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
import mediator.db.favorite_artist.get.Domain.Errors.GetError
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait FavoriteArtistsUsersGetStorage[F[_]] {
  def get(userID: User.ID): F[Either[GetError, Vector[Artist]]]
}

object FavoriteArtistsUsersGetStorage
  extends Logging.Companion[FavoriteArtistsUsersGetStorage] {
  final private class Impl[F[_]: Functor](
      storage: FavoriteArtistsUsersStorage[F]
  ) extends FavoriteArtistsUsersGetStorage[F] {
    override def get(userID: User.ID): F[Either[GetError, Vector[Artist]]] =
      storage.getUserFavoriteArtists(userID).map(vector =>
        Either.cond(vector.nonEmpty, vector, GetError.NotFound)
      )
  }

  final private class LogMid[F[_]: FlatMap: FavoriteArtistsUsersGetStorage.Log]
    extends FavoriteArtistsUsersGetStorage[Mid[F, *]] {
    override def get(userID: User.ID): Mid[F, Either[GetError, Vector[Artist]]] =
      debug"Trying to get user's favorite artists" *>
        _.flatTap {
          case Left(GetError.NotFound) =>
            warn"User's favorite artists are not found"
          case Left(GetError.PSQL(cause)) =>
            errorCause"Failed to get user's favorite artists" (cause)
          case Left(GetError.Connection(cause)) =>
            errorCause"Failed to get user's favorite artists" (cause)
          case Right(_) => debug"Successfully got user's favorite artists"
        }
  }

  private object Errors extends FavoriteArtistsUsersGetStorage[SQLErrorJoiner] {
    override def get(userID: User.ID): SQLErrorJoiner[Either[GetError, Vector[Artist]]] =
      SQLErrorJoiner[Either[GetError, Vector[Artist]]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: FavoriteArtistsUsersGetStorage[F] =
    DatabaseRunner[FavoriteArtistsUsersGetStorage, F].wire(
      new Impl[ConnectionIO](
        FavoriteArtistsUsersStorage.db
      ),
      Errors
    )

  def makeObservable[
      F[_]: MonadCancelThrow: SafeTransactor: Logging.Make
  ]: FavoriteArtistsUsersGetStorage[F] = {
    val logMid = new LogMid[F]: FavoriteArtistsUsersGetStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
