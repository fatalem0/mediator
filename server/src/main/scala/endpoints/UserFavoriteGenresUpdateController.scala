package endpoints

import mediator.Domain.User
import mediator.favorite_genre.Domain.Errors.UpdateUserFavoriteGenresError
import mediator.favorite_genre.Domain.UpdateUserFavoriteGenres
import mediator.favorite_genre.UserFavoriteGenresUpdateService
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.{ endpoint, path, Endpoint }
import utils.errors.ApiError
import utils.server.{ ApiBuilder, HttpModule, WireWithLogic }

trait UserFavoriteGenresUpdateController[F[_]] {
  def update(
      userId: User.ID,
      req: UpdateUserFavoriteGenres.Request
  ): F[Either[UpdateUserFavoriteGenresError, Unit]]
}

object UserFavoriteGenresUpdateController {
  final private class Impl[F[_]](service: UserFavoriteGenresUpdateService[F])
    extends UserFavoriteGenresUpdateController[F] {
    override def update(
        userId: User.ID,
        req: UpdateUserFavoriteGenres.Request
    ): F[Either[UpdateUserFavoriteGenresError, Unit]] = service.update(
      userId,
      req
    )
  }

  object protocol {
    val update: Endpoint[
      Unit,
      (User.ID, UpdateUserFavoriteGenres.Request),
      ApiError,
      Unit,
      Any
    ] = endpoint
      .summary("Обновление списка любимых жанров музыки пользователя")
      .put
      .in(
        ApiV1 / "user" /
          path[User.ID]("userId").description("ID пользователя") /
          "update" / "favorite-genres"
      )
      .in(jsonBody[UpdateUserFavoriteGenres.Request])
      .errorOut(
        ApiError.makeOneOf[UpdateUserFavoriteGenresError](
          UpdateUserFavoriteGenresError.variants
        )
      )
  }

  implicit val wireWithLogic: WireWithLogic[
    UserFavoriteGenresUpdateController
  ] =
    new WireWithLogic[UserFavoriteGenresUpdateController] {
      override def wire[F[_]](
          controller: UserFavoriteGenresUpdateController[F]
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
      service: UserFavoriteGenresUpdateService[F]
  ): UserFavoriteGenresUpdateController[F] = new Impl[F](service)
}
