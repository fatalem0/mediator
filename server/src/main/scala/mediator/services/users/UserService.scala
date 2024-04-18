package mediator.services.users

import cats.effect.Clock
import cats.syntax.flatMap._
import cats.syntax.functor._
import mediator.db.users.Domain.Errors.CheckError
import cats.{ Applicative, Monad }
import derevo.derive
import derevo.tagless.applyK
import io.scalaland.chimney.dsl._
import tofu.generate.GenUUID
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.raise._
import mediator.Domain.UserData.UserId
import mediator.Domain.{ UserData, UserEmail }
import mediator.db.users.Domain.Errors.CreateError
import mediator.db.users.UserStorage
import mediator.services.registration.Domain.Registration
import mediator.services.users.Domain.Errors.UserError

@derive(applyK)
trait UserService[F[_]] {
  def create(req: Registration.Request): F[Either[UserError, Unit]]
}

object UserService {
  final private class Impl[F[_]: Monad: Clock: GenUUID: UserError.Errors](
      userStorage: UserStorage[F]
  ) extends UserService[F] {
    override def create(req: Registration.Request): F[Either[UserError, Unit]] =
      (for {
        _        <- checkIfExists(req.email)
        userData <- createUserData(req)

        created <-
          userStorage
            .create(userData)
            .leftMapIn[UserError] {
              case CreateError.NoUpdate => UserError.NoUpdate(req.email)
              case CreateError.AlreadyExists(_) =>
                UserError.AlreadyExists(req.email)
              case CreateError.PSQL(cause) => UserError.InternalDatabase(cause)
              case CreateError.Connection(cause) => UserError.Internal(cause)
            }
            .reRaise

      } yield created).attempt[UserError]

    private def checkIfExists(email: UserEmail) = {
      userStorage
        .checkIfExistsByEmail(email)
        .catchAll[Boolean] {
          case CheckError.PSQL(cause) =>
            UserError.InternalDatabase(cause).raise[F, Boolean]
          case CheckError.Connection(cause) =>
            UserError.Internal(cause).raise[F, Boolean]
        }
        .ifM(
          ifTrue = UserError.AlreadyExists(email).raise[F, Unit],
          ifFalse = Applicative[F].unit
        )
    }

    private def createUserData(req: Registration.Request) =
      for {
        generatedId <- UserId.create[F]
        now         <- Clock[F].realTimeInstant
        hashedPassword = req.password.hashPassword
      } yield req
        .into[UserData]
        .withFieldConst(_.id, generatedId)
        .withFieldConst(_.hashedPassword, hashedPassword)
        .withFieldConst(_.createdAt, now)
        .withFieldConst(_.updatedAt, now)
        .transform
  }

  def make[F[_]: Monad: Clock: GenUUID: UserError.Errors](
      userStorage: UserStorage[F]
  ): UserService[F] =
    new Impl[F](userStorage)
}
