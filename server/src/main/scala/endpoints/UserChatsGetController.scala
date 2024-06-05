package endpoints

import mediator.Domain.{ Limit, Offset, User }
import mediator.user_chat.Domain.Errors.UserChatsGetError
import mediator.user_chat.Domain.UserChatsGet
import mediator.user_chat.UserChatsGetService
import sttp.tapir._
import sttp.tapir.json.tethysjson.jsonBody
import utils.errors.ApiError
import utils.server.{ ApiBuilder, HttpModule, WireWithLogic }

trait UserChatsGetController[F[_]] {
  def get(
      userId: User.ID,
      limit: Limit,
      offset: Offset
  ): F[Either[UserChatsGetError, UserChatsGet.Response]]
}

object UserChatsGetController {
  final private class Impl[F[_]](service: UserChatsGetService[F])
    extends UserChatsGetController[F] {
    override def get(
        userId: User.ID,
        limit: Limit,
        offset: Offset
    ): F[Either[UserChatsGetError, UserChatsGet.Response]] = service.get(
      limit,
      offset,
      userId
    )
  }

  object protocol {
    val get: Endpoint[
      Unit,
      (User.ID, Limit, Offset),
      ApiError,
      UserChatsGet.Response,
      Any
    ] = endpoint
      .summary("Получение списка чатов пользователя")
      .get
      .in(
        ApiV1 / "user" /
          path[User.ID]("userId").description("ID пользователя") /
          "chats"
      )
      .in(query[Limit]("limit").description(
        "Максимальное количество элементов в ответе"
      ))
      .in(query[Offset]("offset").description(
        "Смещение первого элемента в ответе"
      ))
      .errorOut(
        ApiError.makeOneOf[UserChatsGetError](UserChatsGetError.variants)
      )
      .out(jsonBody[UserChatsGet.Response])
  }

  implicit val wireWithLogic: WireWithLogic[UserChatsGetController] =
    new WireWithLogic[UserChatsGetController] {
      override def wire[F[_]](controller: UserChatsGetController[F])(implicit
          builder: ApiBuilder[F]
      ): HttpModule[F] = List(
        builder.build(
          (controller.get _).tupled,
          protocol.get
        )
      )
    }

  def make[F[_]](service: UserChatsGetService[F]): UserChatsGetController[F] =
    new Impl[F](service)
}
