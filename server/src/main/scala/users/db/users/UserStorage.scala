package users.db.users

import cats.effect.{ Clock, MonadCancelThrow }
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ Applicative, FlatMap }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import tofu.generate.GenUUID
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.foption._
import tofu.syntax.logging._
import users.Domain.{ UserData, UserEmail }
import users.db.users.Domain.Errors.{ CheckError, CreateError, ReadError }
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait UserStorage[F[_]] {
  def findByEmail(email: UserEmail): F[Either[ReadError, UserData]]
  def create(userData: UserData): F[Either[CreateError, Unit]]
  def checkIfExistsByEmail(email: UserEmail): F[Either[CheckError, Boolean]]
}

object UserStorage extends Logging.Companion[UserStorage] {
  final private class Impl[F[_]: Applicative: Clock: GenUUID](
      createStorage: UserCreateStorage[F],
      readStorage: UserReadStorage[F]
  ) extends UserStorage[F] {
    override def findByEmail(email: UserEmail): F[Either[ReadError, UserData]] =
      readStorage.findByEmail(email).toRightIn(ReadError.notFound)

    override def create(userData: UserData): F[Either[CreateError, Unit]] =
      createStorage
        .create(userData)
        .map(Either.cond(_, (), CreateError.NoUpdate))

    override def checkIfExistsByEmail(
        email: UserEmail
    ): F[Either[CheckError, Boolean]] =
      readStorage.findByEmail(email).map(_.isDefined).rightIn[CheckError]
  }

  final private class LogMid[F[_]: FlatMap: UserStorage.Log]
    extends UserStorage[Mid[F, *]] {
    override def findByEmail(
        email: UserEmail
    ): Mid[F, Either[ReadError, UserData]] =
      debug"Trying to fetch user by email = $email" *>
        _.flatTap {
          case Left(ReadError.NotFound) =>
            warn"User with email = $email not found"
          case Left(ReadError.PSQL(cause)) =>
            errorCause"Failed to fetch user by email" (cause)
          case Left(ReadError.Connection(cause)) =>
            errorCause"Failed to fetch user by email due to connection error" (
              cause
            )
          case Right(_) =>
            debug"Successfully fetched user by email = $email"
        }

    override def create(userData: UserData): Mid[F, Either[CreateError, Unit]] =
      debug"Trying to create new user with id = ${userData.id} and email = ${userData.email}" *>
        _.flatTap {
          case Left(CreateError.NoUpdate) =>
            warn"User with id = ${userData.id} and email = ${userData.email} hasn't been created"
          case Left(CreateError.AlreadyExists(_)) =>
            warn"User with email = ${userData.email} already exists"
          case Left(CreateError.PSQL(cause)) =>
            errorCause"Failed to fetch user by email" (cause)
          case Left(CreateError.Connection(cause)) =>
            errorCause"Failed to fetch user by email due to connection error" (
              cause
            )
          case Right(_) =>
            debug"Successfully created new user with id = ${userData.id} and email = ${userData.email}"
        }

    override def checkIfExistsByEmail(
        email: UserEmail
    ): Mid[F, Either[CheckError, Boolean]] =
      debug"Checking if user with email = $email exists" *>
        _.flatTap {
          case Left(CheckError.PSQL(cause)) =>
            errorCause"Failed to fetch user by email" (cause)
          case Left(CheckError.Connection(cause)) =>
            errorCause"Failed to fetch user by email due to connection error" (
              cause
            )
          case Right(true) =>
            debug"Successfully find user with email = $email"
          case Right(false) =>
            debug"Cannot find user with email = $email"
        }
  }

  private object Errors extends UserStorage[SQLErrorJoiner] {
    override def findByEmail(
        email: UserEmail
    ): SQLErrorJoiner[Either[ReadError, UserData]] =
      SQLErrorJoiner[Either[ReadError, UserData]]

    override def create(
        userData: UserData
    ): SQLErrorJoiner[Either[CreateError, Unit]] =
      SQLErrorJoiner[Either[CreateError, Unit]]

    override def checkIfExistsByEmail(
        email: UserEmail
    ): SQLErrorJoiner[Either[CheckError, Boolean]] =
      SQLErrorJoiner[Either[CheckError, Boolean]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor: Clock: GenUUID]
      : UserStorage[F] =
    DatabaseRunner[UserStorage, F].wire(
      new Impl[ConnectionIO](
        UserCreateStorage.db,
        UserReadStorage.db
      ),
      Errors
    )

  def makeObservable[
      F[_]: MonadCancelThrow: SafeTransactor: Clock: GenUUID: Logging.Make
  ]: UserStorage[F] = {
    val logMid = new LogMid[F]: UserStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
