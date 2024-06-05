package endpoints

import mediator.Domain.User
import mediator.favorite_artist.Domain.Errors.UpdateUserFavoriteArtistsError
import mediator.favorite_artist.Domain.UpdateUserFavoriteArtists
import mediator.favorite_artist.UserFavoriteArtistsUpdateService
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.{ endpoint, path, Endpoint }
import utils.errors.ApiError
import utils.server.{ ApiBuilder, HttpModule, WireWithLogic }

trait UserFavoriteArtistsUpdateController[F[_]] {
  def update(
      userId: User.ID,
      req: UpdateUserFavoriteArtists.Request
  ): F[Either[UpdateUserFavoriteArtistsError, Unit]]
}

object UserFavoriteArtistsUpdateController {
  final private class Impl[F[_]](service: UserFavoriteArtistsUpdateService[F])
    extends UserFavoriteArtistsUpdateController[F] {
    override def update(
        userId: User.ID,
        req: UpdateUserFavoriteArtists.Request
    ): F[Either[UpdateUserFavoriteArtistsError, Unit]] = service.update(
      userId,
      req
    )
  }

  object protocol {
    val update: Endpoint[
      Unit,
      (User.ID, UpdateUserFavoriteArtists.Request),
      ApiError,
      Unit,
      Any
    ] = endpoint
      .summary("Обновление списка любимых исполнителей пользователя")
      .put
      .in(
        ApiV1 / "user" /
          path[User.ID]("userId").description("ID пользователя") /
          "update" / "favorite-artists"
      )
      .in(jsonBody[UpdateUserFavoriteArtists.Request])
      .errorOut(
        ApiError.makeOneOf[UpdateUserFavoriteArtistsError](
          UpdateUserFavoriteArtistsError.variants
        )
      )
  }

  implicit val wireWithLogic: WireWithLogic[
    UserFavoriteArtistsUpdateController
  ] =
    new WireWithLogic[UserFavoriteArtistsUpdateController] {
      override def wire[F[_]](
          controller: UserFavoriteArtistsUpdateController[F]
      )(implicit
          builder: ApiBuilder[F]
      ): HttpModule[F] = List(
        builder.build(
          (controller.update _).tupled,
          protocol.update
        )
      )
    }

  def make[F[_]](
      service: UserFavoriteArtistsUpdateService[F]
  ): UserFavoriteArtistsUpdateController[F] = new Impl[F](service)
}
