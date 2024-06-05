package mediator._stubs

import cats.effect.IO
import mediator.Domain.{ User, UserPurpose }
import mediator.db.user_purpose.get.Domain.Errors
import mediator.db.user_purpose.get.Domain.Errors.GetError
import mediator.db.user_purpose.get.UserPurposesGetStorage

final class UserPurposesGetStorageStub(
    response: Either[Errors.GetError, Vector[UserPurpose]]
) extends UserPurposesGetStorage[IO] {
  override def get: IO[Either[Errors.GetError, Vector[UserPurpose]]] =
    IO.pure(response)

  override def getByUserID(userID: User.ID): IO[Either[
    GetError,
    Vector[UserPurpose]
  ]] = IO.pure(response)
}
