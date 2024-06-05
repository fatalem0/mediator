package mediator.user_purpose

import cats.effect.unsafe.implicits.global
import mediator.Domain.UserPurpose
import mediator._stubs.UserPurposesGetStorageStub
import mediator.db.user_purpose.get.Domain.Errors.GetError
import mediator.user_purpose.get.Domain.Errors.UserPurposesGetError
import mediator.user_purpose.get.UserPurposesGetService
import org.postgresql.util.{ PSQLException, PSQLState }
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.errors.ErrorLevel

import java.util.UUID

class UserPurposesGetServiceSpec extends AnyFlatSpec with Matchers
    with EitherValues {
  "getUserPurposes" should "successfully return vector of user purposes" in {
    val userPurposes = Vector(
      UserPurpose(
        id = UserPurpose.ID(UUID.randomUUID()),
        name = UserPurpose.Name("Пойти на концерт")
      )
    )

    val userPurposeGetStorage =
      new UserPurposesGetStorageStub(Right(userPurposes))
    val userPurposeGetService = UserPurposesGetService.make(
      userPurposeGetStorage
    )

    userPurposeGetService.getUserPurposes.unsafeRunSync().value shouldBe userPurposes
  }

  it should "return NotFound error" in {
    val storageErrorResponse = Left(GetError.NotFound)
    val userPurposesGetStorage =
      new UserPurposesGetStorageStub(storageErrorResponse)
    val userPurposesGetService = UserPurposesGetService.make(
      userPurposesGetStorage
    )

    userPurposesGetService.getUserPurposes.unsafeRunSync().left.value shouldBe a[
      UserPurposesGetError.NotFound.type
    ]
    userPurposesGetService.getUserPurposes.unsafeRunSync().left.value.code shouldBe "USER_PURPOSES_GET_NOT_FOUND"
    userPurposesGetService.getUserPurposes.unsafeRunSync().left.value.level shouldBe ErrorLevel.NotFound
  }

  it should "return InternalDatabase error" in {
    val storageErrorResponse = Left(GetError.PSQL(new PSQLException(
      "ERROR",
      PSQLState.SYSTEM_ERROR
    )))
    val userPurposesGetStorage =
      new UserPurposesGetStorageStub(storageErrorResponse)
    val userPurposesGetService = UserPurposesGetService.make(
      userPurposesGetStorage
    )

    userPurposesGetService.getUserPurposes.unsafeRunSync().left.value shouldBe a[
      UserPurposesGetError.InternalDatabase
    ]
    userPurposesGetService.getUserPurposes.unsafeRunSync().left.value.code shouldBe "USER_PURPOSES_GET_INTERNAL_DATABASE"
    userPurposesGetService.getUserPurposes.unsafeRunSync().left.value.level shouldBe ErrorLevel.Internal
  }

  it should "return Internal error" in {
    val storageErrorResponse = Left(
      GetError.Connection(new NullPointerException())
    )
    val userPurposesGetStorage =
      new UserPurposesGetStorageStub(storageErrorResponse)
    val userPurposesGetService = UserPurposesGetService.make(
      userPurposesGetStorage
    )

    userPurposesGetService.getUserPurposes.unsafeRunSync().left.value shouldBe a[
      UserPurposesGetError.Internal
    ]
    userPurposesGetService.getUserPurposes.unsafeRunSync().left.value.code shouldBe "USER_PURPOSES_GET_INTERNAL"
    userPurposesGetService.getUserPurposes.unsafeRunSync().left.value.level shouldBe ErrorLevel.Internal
  }
}
