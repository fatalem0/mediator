package mediator.db.city

import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Functor }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.Domain.City
import mediator.db.city.Domain.Errors.GetError
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.foption._
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait CitiesGetStorage[F[_]] {
  def getCities: F[Either[GetError, Vector[City]]]
  def getByID(cityID: City.ID): F[Either[GetError, City]]
}

object CitiesGetStorage extends Logging.Companion[CitiesGetStorage] {
  final private class Impl[F[_]: Functor](storage: CitiesStorage[F])
    extends CitiesGetStorage[F] {
    override def getCities: F[Either[GetError, Vector[City]]] =
      storage.getCities.map(vector =>
        Either.cond(vector.nonEmpty, vector, GetError.NotFound)
      )

    override def getByID(cityID: City.ID): F[Either[GetError, City]] =
      storage.getByID(cityID).toRightIn(GetError.NotFound)
  }

  final private class LogMid[F[_]: FlatMap: CitiesGetStorage.Log]
    extends CitiesGetStorage[Mid[F, *]] {
    override def getCities: Mid[F, Either[GetError, Vector[City]]] =
      debug"Trying to get cities" *>
        _.flatTap {
          case Left(GetError.NotFound) => warn"Cities are not found"
          case Left(GetError.PSQL(cause)) =>
            errorCause"Failed to get cities" (cause)
          case Left(GetError.Connection(cause)) =>
            errorCause"Failed to get cities" (cause)
          case Right(_) => debug"Successfully got cities"
        }

    override def getByID(cityID: City.ID): Mid[F, Either[GetError, City]] =
      debug"Trying to get city by cityID = $cityID" *>
        _.flatTap {
          case Left(GetError.NotFound) =>
            warn"City by cityID = $cityID not found"
          case Left(GetError.PSQL(cause)) =>
            errorCause"Failed to get city by cityID = $cityID" (cause)
          case Left(GetError.Connection(cause)) =>
            errorCause"Failed to get city by cityID = $cityID due to connection error" (
              cause
            )
          case Right(_) => debug"Successfully got city by cityID = $cityID"
        }
  }

  private object Errors extends CitiesGetStorage[SQLErrorJoiner] {
    override def getCities: SQLErrorJoiner[Either[GetError, Vector[City]]] =
      SQLErrorJoiner[Either[GetError, Vector[City]]]

    override def getByID(cityID: City.ID): SQLErrorJoiner[Either[GetError, City]] =
      SQLErrorJoiner[Either[GetError, City]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: CitiesGetStorage[F] =
    DatabaseRunner[CitiesGetStorage, F].wire(
      new Impl[ConnectionIO](
        CitiesStorage.db
      ),
      Errors
    )

  def makeObservable[
      F[_]: MonadCancelThrow: SafeTransactor: Logging.Make
  ]: CitiesGetStorage[F] = {
    val logMid = new LogMid[F]: CitiesGetStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
