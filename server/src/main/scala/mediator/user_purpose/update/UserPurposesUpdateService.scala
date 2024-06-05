package mediator.user_purpose.update

import cats.data.NonEmptyVector
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Monad }
import derevo.derive
import derevo.tagless.applyK
import mediator.Domain.User
import mediator.db.user_purpose.update.Domain.Errors.UpdateError
import mediator.db.user_purpose.update.UserPurposesUpdateStorage
import Domain.Errors.UpdateUserPurposesError
import Domain.Errors.UpdateUserPurposesError.NoUserPurposes
import Domain.UpdateUserPurposes
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.logging._
import tofu.syntax.raise._

@derive(applyK)
trait UserPurposesUpdateService[F[_]] {
  def update(
      userId: User.ID,
      req: UpdateUserPurposes.Request
  ): F[Either[UpdateUserPurposesError, Unit]]
}

object UserPurposesUpdateService
  extends Logging.Companion[UserPurposesUpdateService] {
  final private class Impl[F[_]: Monad: UpdateUserPurposesError.Errors](
      storage: UserPurposesUpdateStorage[F]
  ) extends UserPurposesUpdateService[F] {
    override def update(
        userId: User.ID,
        req: UpdateUserPurposes.Request
    ): F[Either[UpdateUserPurposesError, Unit]] =
      (for {
        _ <- NoUserPurposes.raise[F, Unit].whenA(req.userPurposeIds.isEmpty)
        userPurposeIds = NonEmptyVector.fromVectorUnsafe(req.userPurposeIds)
        _ <-
          storage.update(userId, userPurposeIds)
            .leftMapIn[UpdateUserPurposesError] {
              case UpdateError.ReferenceNotFound(_) | UpdateError.NoUpdate =>
                UpdateUserPurposesError.UserPurposesNotUpdated
              case UpdateError.PSQL(cause) =>
                UpdateUserPurposesError.InternalDatabase(cause)
              case UpdateError.Connection(cause) =>
                UpdateUserPurposesError.Internal(cause)
            }
            .reRaise
      } yield ()).attempt[UpdateUserPurposesError]
  }

  final private class LogMid[F[_]: FlatMap: UserPurposesUpdateService.Log]
    extends UserPurposesUpdateService[Mid[F, *]] {
    override def update(
        userId: User.ID,
        req: UpdateUserPurposes.Request
    ): Mid[F, Either[UpdateUserPurposesError, Unit]] =
      debug"Updating list of user purposes for user with id = $userId" *>
        _.flatTap {
          case Left(error) =>
            error"Failed to update list of user purposes for user with id = $userId. $error"
          case Right(_) =>
            debug"Successfully updated list of user purposes for user with id = $userId"
        }
  }

  def make[F[_]: Monad: UpdateUserPurposesError.Errors](
      storage: UserPurposesUpdateStorage[F]
  ): UserPurposesUpdateService[F] = new Impl[F](storage)

  def makeObservable[
      F[_]: Monad: UpdateUserPurposesError.Errors: UserPurposesUpdateService.Log
  ](
      storage: UserPurposesUpdateStorage[F]
  ): UserPurposesUpdateService[F] = new LogMid[F] attach make[F](storage)
}
