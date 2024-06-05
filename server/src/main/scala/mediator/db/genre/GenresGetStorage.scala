package mediator.db.genre

import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Functor }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.Domain.Genre
import mediator.db.genre.Domain.Errors.GetError
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait GenresGetStorage[F[_]] {
  def getGenres: F[Either[GetError, Vector[Genre]]]
}

object GenresGetStorage extends Logging.Companion[GenresGetStorage] {
  final private class Impl[F[_]: Functor](storage: GenresStorage[F])
    extends GenresGetStorage[F] {
    override def getGenres: F[Either[GetError, Vector[Genre]]] =
      storage.getGenres.map(vector =>
        Either.cond(vector.nonEmpty, vector, GetError.NotFound)
      )
  }

  final private class LogMid[F[_]: FlatMap: GenresGetStorage.Log]
    extends GenresGetStorage[Mid[F, *]] {
    override def getGenres: Mid[F, Either[GetError, Vector[Genre]]] =
      debug"Trying to get genres" *>
        _.flatTap {
          case Left(GetError.NotFound) => warn"Genres are not found"
          case Left(GetError.PSQL(cause)) =>
            errorCause"Failed to get genres" (cause)
          case Left(GetError.Connection(cause)) =>
            errorCause"Failed to get genres" (cause)
          case Right(_) => debug"Successfully got genres"
        }
  }

  private object Errors extends GenresGetStorage[SQLErrorJoiner] {
    override def getGenres: SQLErrorJoiner[Either[GetError, Vector[Genre]]] =
      SQLErrorJoiner[Either[GetError, Vector[Genre]]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: GenresGetStorage[F] =
    DatabaseRunner[GenresGetStorage, F].wire(
      new Impl[ConnectionIO](
        GenresStorage.db
      ),
      Errors
    )

  def makeObservable[F[_]: MonadCancelThrow: SafeTransactor: Logging.Make]: GenresGetStorage[F] = {
    val logMid = new LogMid[F]: GenresGetStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
