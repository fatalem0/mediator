package endpoints

import mediator.Domain.{ Limit, Offset, User }
import mediator.potential_friend.Domain.Errors.UserPotentialFriendsGetError
import mediator.potential_friend.Domain.{ GetPotentialFriends, PotentialFriend }
import mediator.potential_friend.PotentialFriendsGetService
import sttp.tapir._
import sttp.tapir.json.tethysjson.jsonBody
import utils.errors.ApiError
import utils.server.{ ApiBuilder, HttpModule, WireWithLogic }

trait UserPotentialFriendsGetController[F[_]] {
  def getUserPotentialFriends(
      userId: User.ID,
      limit: Limit,
      offset: Offset
  ): F[Either[UserPotentialFriendsGetError, GetPotentialFriends.Response]]
}

object UserPotentialFriendsGetController {
  final private class Impl[F[_]](service: PotentialFriendsGetService[F])
    extends UserPotentialFriendsGetController[F] {
    override def getUserPotentialFriends(
        userId: User.ID,
        limit: Limit,
        offset: Offset
    ): F[Either[UserPotentialFriendsGetError, GetPotentialFriends.Response]] =
      service.getUserPotentialFriends(userId, limit, offset)
  }

  object protocol {
    val get: Endpoint[
      Unit,
      (User.ID, Limit, Offset),
      ApiError,
      GetPotentialFriends.Response,
      Any
    ] = endpoint
      .summary("Получение списка потенциальных друзей пользователя")
      .get
      .in(
        ApiV1 / "user" /
          path[User.ID]("userId").description("ID пользователя") /
          "potential-friends"
      )
      .in(query[Limit]("limit").description(
        "Максимальное количество элементов в ответе"
      ))
      .in(query[Offset]("offset").description(
        "Смещение первого элемента в ответе"
      ))
      .errorOut(
        ApiError.makeOneOf[UserPotentialFriendsGetError](
          UserPotentialFriendsGetError.variants
        )
      )
      .out(jsonBody[GetPotentialFriends.Response])
  }

  implicit val wireWithLogic: WireWithLogic[UserPotentialFriendsGetController] =
    new WireWithLogic[UserPotentialFriendsGetController] {
      override def wire[F[_]](controller: UserPotentialFriendsGetController[F])(
          implicit builder: ApiBuilder[F]
      ): HttpModule[F] = List(
        builder.build(
          (controller.getUserPotentialFriends _).tupled,
          protocol.get
        )
      )
    }

  def make[F[_]](
      service: PotentialFriendsGetService[F]
  ): UserPotentialFriendsGetController[F] = new Impl[F](service)
}
