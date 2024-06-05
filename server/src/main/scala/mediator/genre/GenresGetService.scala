package mediator.genre

import cats.Functor
import derevo.derive
import derevo.tagless.applyK
import mediator.Domain.Genre
import mediator.db.genre.Domain.Errors.GetError
import mediator.db.genre.GenresGetStorage
import mediator.genre.Domain.Errors.GenresGetError
import tofu.logging.Logging
import tofu.syntax.feither._

@derive(applyK)
trait GenresGetService[F[_]] {
  def getGenres: F[Either[GenresGetError, Vector[Genre]]]
}

object GenresGetService extends Logging.Companion[GenresGetService] {
  final private class Impl[F[_]: Functor](storage: GenresGetStorage[F])
    extends GenresGetService[F] {
    override def getGenres: F[Either[GenresGetError, Vector[Genre]]] =
      storage.getGenres
        .leftMapIn[GenresGetError] {
          case GetError.NotFound          => GenresGetError.NotFound
          case GetError.PSQL(cause)       => GenresGetError.InternalDatabase(cause)
          case GetError.Connection(cause) => GenresGetError.Internal(cause)
        }
  }

  def make[F[_]: Functor](storage: GenresGetStorage[F]): GenresGetService[F] =
    new Impl[F](storage)
}
