package mediator.user.update

import cats.effect.Clock
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Monad }
import derevo.derive
import derevo.tagless.applyK
import io.scalaland.chimney.dsl._
import mediator.Domain.User
import mediator.db.user.update.Domain.Errors.UpdateError
import mediator.db.user.update.Domain.UpdateUserRow
import mediator.db.user.update.UserUpdateStorage
import mediator.user.update.Domain.Errors.UpdateUserError
import mediator.user.update.Domain.UpdateUser
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.logging._

@derive(applyK)
trait UserUpdateService[F[_]] {
  def update(
      userId: User.ID,
      req: UpdateUser.Request
  ): F[Either[UpdateUserError, Unit]]
}

object UserUpdateService extends Logging.Companion[UserUpdateService] {
  final private class Impl[F[_]: Monad: Clock: UpdateUserError.Errors](
      storage: UserUpdateStorage[F]
  ) extends UserUpdateService[F] {
    override def update(
        userId: User.ID,
        req: UpdateUser.Request
    ): F[Either[UpdateUserError, Unit]] =
      (for {
        updateUserRow <- createUserRowForUpdate(req)

        _ <-
          storage.update(userId, updateUserRow)
            .leftMapIn[UpdateUserError] {
              case UpdateError.NoUpdate => UpdateUserError.UserNotUpdated
              case UpdateError.PSQL(cause) =>
                UpdateUserError.InternalDatabase(cause)
              case UpdateError.Connection(cause) =>
                UpdateUserError.Internal(cause)
            }
            .reRaise
      } yield ()).attempt[UpdateUserError]

    private def createUserRowForUpdate(
        req: UpdateUser.Request
    ): F[UpdateUserRow] =
      for {
        now <- Clock[F].realTimeInstant
      } yield req
        .into[UpdateUserRow]
        .withFieldConst(_.hashedPassword, req.password.map(_.hashPassword))
        .withFieldConst(_.updatedAt, now)
        .transform
  }

  final private class LogMid[F[_]: FlatMap: UserUpdateService.Log]
    extends UserUpdateService[Mid[F, *]] {
    override def update(
        userId: User.ID,
        req: UpdateUser.Request
    ): Mid[F, Either[UpdateUserError, Unit]] =
      debug"Updating user with id = $userId" *>
        _.flatTap {
          case Left(error) =>
            error"Failed to update user with id = $userId. $error"
          case Right(_) => debug"Successfully updated user with id = $userId"
        }
  }

  def make[F[_]: Monad: Clock: UpdateUserError.Errors](
      storage: UserUpdateStorage[F]
  ): UserUpdateService[F] = new Impl[F](storage)

  def makeObservable[F[_]: Monad: Clock: UpdateUserError.Errors: UserUpdateService.Log](
      storage: UserUpdateStorage[F]
  ): UserUpdateService[F] = new LogMid[F] attach make[F](storage)
}
