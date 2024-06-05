package mediator.db.user.create

import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Functor }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.db.user.UserStorage
import mediator.db.user.create.Domain.CreateUserRow
import mediator.db.user.create.Domain.Errors.CreateError
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait UserCreateStorage[F[_]] {
  def create(row: CreateUserRow): F[Either[CreateError, Unit]]
}

object UserCreateStorage extends Logging.Companion[UserCreateStorage] {
  final private class Impl[F[_]: Functor](storage: UserStorage[F])
    extends UserCreateStorage[F] {
    override def create(row: CreateUserRow): F[Either[CreateError, Unit]] =
      storage.create(row).map(Either.cond(_, (), CreateError.NoUpdate))
  }

  final private class LogMid[F[_]: FlatMap: UserCreateStorage.Log]
    extends UserCreateStorage[Mid[F, *]] {
    override def create(row: CreateUserRow): Mid[F, Either[CreateError, Unit]] =
      debug"Trying to create user with id = ${row.id} and email = ${row.email}" *>
        _.flatTap {
          case Left(CreateError.NoUpdate) =>
            warn"User with id = ${row.id} and email = ${row.email} hasn't been created"
          case Left(CreateError.AlreadyExists(_)) =>
            warn"User with id = ${row.id} and email = ${row.email} already exists"
          case Left(CreateError.PSQL(cause)) =>
            errorCause"Failed to create user with id = ${row.id} and email = ${row.email}" (
              cause
            )
          case Left(CreateError.Connection(cause)) =>
            errorCause"Failed to create user with id = ${row.id} and email = ${row.email} due to connection error" (
              cause
            )
          case Right(_) =>
            debug"Successfully created user with id = ${row.id} and email = ${row.email}"
        }
  }

  private object Errors extends UserCreateStorage[SQLErrorJoiner] {
    override def create(row: CreateUserRow): SQLErrorJoiner[Either[CreateError, Unit]] =
      SQLErrorJoiner[Either[CreateError, Unit]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: UserCreateStorage[F] =
    DatabaseRunner[UserCreateStorage, F].wire(
      new Impl[ConnectionIO](
        UserStorage.db
      ),
      Errors
    )

  def makeObservable[F[_]: MonadCancelThrow: SafeTransactor: Logging.Make]: UserCreateStorage[F] = {
    val logMid = new LogMid[F]: UserCreateStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
