package mediator.user_purpose.get

import cats.Functor
import derevo.derive
import derevo.tagless.applyK
import mediator.Domain.UserPurpose
import mediator.db.user_purpose.get.Domain.Errors.GetError
import mediator.db.user_purpose.get.UserPurposesGetStorage
import mediator.user_purpose.get.Domain.Errors.UserPurposesGetError
import tofu.logging.Logging
import tofu.syntax.feither._

@derive(applyK)
trait UserPurposesGetService[F[_]] {
  def getUserPurposes: F[Either[UserPurposesGetError, Vector[UserPurpose]]]
}

object UserPurposesGetService
  extends Logging.Companion[UserPurposesGetService] {
  final private class Impl[F[_]: Functor](storage: UserPurposesGetStorage[F])
    extends UserPurposesGetService[F] {
    override def getUserPurposes: F[Either[UserPurposesGetError, Vector[UserPurpose]]] =
      storage.get
        .leftMapIn[UserPurposesGetError] {
          case GetError.NotFound => UserPurposesGetError.NotFound
          case GetError.PSQL(cause) =>
            UserPurposesGetError.InternalDatabase(cause)
          case GetError.Connection(cause) =>
            UserPurposesGetError.Internal(cause)
        }
  }

  def make[F[_]: Functor](
      storage: UserPurposesGetStorage[F]
  ): UserPurposesGetService[F] = new Impl[F](storage)
}
