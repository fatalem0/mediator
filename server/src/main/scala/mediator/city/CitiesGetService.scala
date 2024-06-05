package mediator.city

import cats.Functor
import derevo.derive
import derevo.tagless.applyK
import mediator.Domain.City
import mediator.city.Domain.Errors.CitiesGetError
import mediator.db.city.CitiesGetStorage
import mediator.db.city.Domain.Errors.GetError
import tofu.logging.Logging
import tofu.syntax.feither._

@derive(applyK)
trait CitiesGetService[F[_]] {
  def getCities: F[Either[CitiesGetError, Vector[City]]]
}

object CitiesGetService extends Logging.Companion[CitiesGetService] {
  final private class Impl[F[_]: Functor](storage: CitiesGetStorage[F])
    extends CitiesGetService[F] {
    override def getCities: F[Either[CitiesGetError, Vector[City]]] =
      storage.getCities
        .leftMapIn[CitiesGetError] {
          case GetError.NotFound          => CitiesGetError.NotFound
          case GetError.PSQL(cause)       => CitiesGetError.InternalDatabase(cause)
          case GetError.Connection(cause) => CitiesGetError.Internal(cause)
        }
  }

  def make[F[_]: Functor](storage: CitiesGetStorage[F]): CitiesGetService[F] =
    new Impl[F](storage)
}
