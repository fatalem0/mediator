package endpoints

import mediator.Domain.User
import mediator.user_purpose.update.Domain.Errors.UpdateUserPurposesError
import mediator.user_purpose.update.Domain.UpdateUserPurposes
import mediator.user_purpose.update.UserPurposesUpdateService
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.{ endpoint, path, Endpoint }
import utils.errors.ApiError
import utils.server.{ ApiBuilder, HttpModule, WireWithLogic }

trait UserPurposesUpdateController[F[_]] {
  def update(
      userId: User.ID,
      req: UpdateUserPurposes.Request
  ): F[Either[UpdateUserPurposesError, Unit]]
}

object UserPurposesUpdateController {
  final private class Impl[F[_]](service: UserPurposesUpdateService[F])
    extends UserPurposesUpdateController[F] {
    override def update(
        userId: User.ID,
        req: UpdateUserPurposes.Request
    ): F[Either[UpdateUserPurposesError, Unit]] = service.update(userId, req)
  }

  object protocol {
    val update: Endpoint[
      Unit,
      (User.ID, UpdateUserPurposes.Request),
      ApiError,
      Unit,
      Any
    ] = endpoint
      .summary("Обновление списка целей использования приложения")
      .put
      .in(
        ApiV1 / "user" /
          path[User.ID]("userId").description("ID пользователя") /
          "update" / "user-purposes"
      )
      .in(jsonBody[UpdateUserPurposes.Request])
      .errorOut(
        ApiError.makeOneOf[UpdateUserPurposesError](
          UpdateUserPurposesError.variants
        )
      )
  }

  implicit val wireWithLogic: WireWithLogic[UserPurposesUpdateController] =
    new WireWithLogic[UserPurposesUpdateController] {
      override def wire[F[_]](controller: UserPurposesUpdateController[F])(
          implicit builder: ApiBuilder[F]
      ): HttpModule[F] = List(
        builder.build(
          (controller.update _).tupled,
          protocol.update
        )
      )
    }

  def make[F[_]](
      service: UserPurposesUpdateService[F]
  ): UserPurposesUpdateController[F] = new Impl[F](service)
}
