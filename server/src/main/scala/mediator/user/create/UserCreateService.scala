package mediator.user.create

import cats.effect.Clock
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ Applicative, Monad }
import derevo.derive
import derevo.tagless.applyK
import io.scalaland.chimney.dsl._
import mediator.Domain.User
import mediator.db.user.check.Domain.Errors.CheckError
import mediator.db.user.check.UserCheckStorage
import mediator.db.user.create.Domain.CreateUserRow
import mediator.db.user.create.Domain.Errors.CreateError
import mediator.db.user.create.UserCreateStorage
import mediator.registration.Domain.Registration
import mediator.user.create.Domain.Errors.UserCreateError
import tofu.generate.GenUUID
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.raise._

@derive(applyK)
trait UserCreateService[F[_]] {
  def create(req: Registration.Request): F[Either[UserCreateError, User.ID]]
}

object UserCreateService {
  final private class Impl[F[_]: Monad: Clock: GenUUID: UserCreateError.Errors](
      userCreateStorage: UserCreateStorage[F],
      userCheckStorage: UserCheckStorage[F]
  ) extends UserCreateService[F] {
    override def create(req: Registration.Request): F[Either[UserCreateError, User.ID]] =
      (for {
        _   <- checkIfExists(req.email)
        row <- createUserRow(req)

        _ <-
          userCreateStorage
            .create(row)
            .leftMapIn[UserCreateError] {
              case CreateError.NoUpdate => UserCreateError.NoUpdate(req.email)
              case CreateError.AlreadyExists(_) =>
                UserCreateError.AlreadyExists(req.email)
              case CreateError.PSQL(cause) =>
                UserCreateError.InternalDatabase(cause)
              case CreateError.Connection(cause) =>
                UserCreateError.Internal(cause)
            }
            .reRaise

      } yield row.id).attempt[UserCreateError]

    private def checkIfExists(email: User.Email): F[Unit] = {
      userCheckStorage
        .checkIfExistsByEmail(email)
        .catchAll[Boolean] {
          case CheckError.PSQL(cause) =>
            UserCreateError.InternalDatabase(cause).raise[F, Boolean]
          case CheckError.Connection(cause) =>
            UserCreateError.Internal(cause).raise[F, Boolean]
        }
        .ifM(
          ifTrue = UserCreateError.AlreadyExists(email).raise[F, Unit],
          ifFalse = Applicative[F].unit
        )
    }

    private def createUserRow(req: Registration.Request): F[CreateUserRow] =
      for {
        generatedId <- User.ID.create[F]
        now         <- Clock[F].realTimeInstant
        hashedPassword = req.password.hashPassword
      } yield req
        .into[CreateUserRow]
        .withFieldConst(_.id, generatedId)
        .withFieldConst(_.hashedPassword, hashedPassword)
        .withFieldConst(_.createdAt, now)
        .withFieldConst(_.updatedAt, now)
        .transform
  }

  def make[F[_]: Monad: Clock: GenUUID: UserCreateError.Errors](
      userCreateStorage: UserCreateStorage[F],
      userCheckStorage: UserCheckStorage[F]
  ): UserCreateService[F] = new Impl[F](userCreateStorage, userCheckStorage)
}
