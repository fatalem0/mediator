package mediator.db.user_purpose.get

import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Functor }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.Domain.{ User, UserPurpose }
import Domain.Errors.GetError
import mediator.db.user_purpose.UserPurposesStorage
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait UserPurposesGetStorage[F[_]] {
  def get: F[Either[GetError, Vector[UserPurpose]]]
  def getByUserID(userID: User.ID): F[Either[GetError, Vector[UserPurpose]]]
}

object UserPurposesGetStorage
  extends Logging.Companion[UserPurposesGetStorage] {
  final private class Impl[F[_]: Functor](storage: UserPurposesStorage[F])
    extends UserPurposesGetStorage[F] {
    override def get: F[Either[GetError, Vector[UserPurpose]]] =
      storage.get.map(vector =>
        Either.cond(vector.nonEmpty, vector, GetError.NotFound)
      )

    override def getByUserID(userID: User.ID): F[Either[
      GetError,
      Vector[UserPurpose]
    ]] = storage.getByUserID(userID).map(vector =>
      Either.cond(vector.nonEmpty, vector, GetError.NotFound)
    )
  }

  final private class LogMid[F[_]: FlatMap: UserPurposesGetStorage.Log]
    extends UserPurposesGetStorage[Mid[F, *]] {
    override def get: Mid[F, Either[GetError, Vector[UserPurpose]]] =
      debug"Trying to get user purposes" *>
        _.flatTap {
          case Left(GetError.NotFound) => warn"User purposes are not found"
          case Left(GetError.PSQL(cause)) =>
            errorCause"Failed to get user purposes" (cause)
          case Left(GetError.Connection(cause)) =>
            errorCause"Failed to get user purposes" (cause)
          case Right(_) => debug"Successfully got user purposes"
        }

    override def getByUserID(userID: User.ID): Mid[
      F,
      Either[GetError, Vector[UserPurpose]]
    ] =
      debug"Trying to get user purposes by userID = $userID" *>
        _.flatTap {
          case Left(GetError.NotFound) =>
            warn"User purposes by userID = $userID are not found"
          case Left(GetError.PSQL(cause)) =>
            errorCause"Failed to get user purposes by userID = $userID" (cause)
          case Left(GetError.Connection(cause)) =>
            errorCause"Failed to get user purposes by userID = $userID" (cause)
          case Right(_) =>
            debug"Successfully got user purposes by userID = $userID"
        }
  }

  private object Errors extends UserPurposesGetStorage[SQLErrorJoiner] {
    override def get: SQLErrorJoiner[Either[GetError, Vector[UserPurpose]]] =
      SQLErrorJoiner[Either[GetError, Vector[UserPurpose]]]

    override def getByUserID(userID: User.ID): SQLErrorJoiner[Either[
      GetError,
      Vector[UserPurpose]
    ]] =
      SQLErrorJoiner[Either[GetError, Vector[UserPurpose]]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: UserPurposesGetStorage[F] =
    DatabaseRunner[UserPurposesGetStorage, F].wire(
      new Impl[ConnectionIO](
        UserPurposesStorage.db
      ),
      Errors
    )

  def makeObservable[F[_]: MonadCancelThrow: SafeTransactor: Logging.Make]: UserPurposesGetStorage[
    F
  ] = {
    val logMid = new LogMid[F]: UserPurposesGetStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
