package endpoints

import mediator.Domain.User
import mediator.user.update.Domain.Errors.UpdateUserError
import mediator.user.update.Domain.UpdateUser
import mediator.user.update.UserUpdateService
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.{ endpoint, path, Endpoint }
import utils.errors.ApiError
import utils.server.{ ApiBuilder, HttpModule, WireWithLogic }

trait UserUpdateController[F[_]] {
  def update(
      userId: User.ID,
      req: UpdateUser.Request
  ): F[Either[UpdateUserError, Unit]]
}

object UserUpdateController {
  final private class Impl[F[_]](service: UserUpdateService[F])
    extends UserUpdateController[F] {
    override def update(
        userId: User.ID,
        req: UpdateUser.Request
    ): F[Either[UpdateUserError, Unit]] = service.update(userId, req)
  }

  object protocol {
    val update: Endpoint[
      Unit,
      (User.ID, UpdateUser.Request),
      ApiError,
      Unit,
      Any
    ] = endpoint
      .summary("Обновление информации о пользователе")
      .put
      .in(
        ApiV1 / "user" /
          path[User.ID]("userId").description("ID пользователя") /
          "update"
      )
      .in(jsonBody[UpdateUser.Request])
      .errorOut(
        ApiError.makeOneOf[UpdateUserError](UpdateUserError.variants)
      )
  }

  implicit val wireWithLogic: WireWithLogic[UserUpdateController] =
    new WireWithLogic[UserUpdateController] {
      override def wire[F[_]](controller: UserUpdateController[F])(implicit
          builder: ApiBuilder[F]
      ): HttpModule[F] = List(
        builder.build(
          (controller.update _).tupled,
          protocol.update
        )
      )
    }

  def make[F[_]](service: UserUpdateService[F]): UserUpdateController[F] =
    new Impl[F](service)
}
