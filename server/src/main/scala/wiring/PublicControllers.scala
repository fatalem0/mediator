package wiring

import cats.Applicative
import endpoints._
import utils.server._

class PublicControllers[F[_]](implicit
    val registration: RegistrationController[F],
    val login: LoginController[F],
    val userUpdate: UserUpdateController[F],
    val userFavoriteArtistsUpdate: UserFavoriteArtistsUpdateController[F],
    val userFavoriteGenresUpdate: UserFavoriteGenresUpdateController[F],
    val userPurposesUpdate: UserPurposesUpdateController[F],
    val userPotentialFriendsGet: UserPotentialFriendsGetController[F],
    val artistsGet: ArtistsGetController[F],
    val userPurposesGet: UserPurposesGetController[F],
    val genresGet: GenresGetController[F],
    val citiesGet: CitiesGetController[F],
    val chatCreate: ChatCreateController[F],
    val chatsGet: UserChatsGetController[F]
)

object PublicControllers {
  implicit val wire: WireWithLogic[PublicControllers] =
    new WireWithLogic[PublicControllers] {
      override def wire[F[_]](controllers: PublicControllers[F])(implicit
          builder: ApiBuilder[F]
      ): HttpModule[F] = {
        import controllers._

        registration.wire ++
          login.wire ++
          userUpdate.wire ++
          userFavoriteArtistsUpdate.wire ++
          userFavoriteGenresUpdate.wire ++
          userPurposesUpdate.wire ++
          userPotentialFriendsGet.wire ++
          artistsGet.wire ++
          userPurposesGet.wire ++
          genresGet.wire ++
          citiesGet.wire ++
          chatCreate.wire ++
          chatsGet.wire
      }
    }

  def make[F[_]: Applicative](
      services: ServiceComponent[F]
  ): PublicControllers[F] = {
    implicit val registrationController: RegistrationController[F] =
      RegistrationController.make[F](services.registration)

    implicit val loginController: LoginController[F] =
      LoginController.make[F](services.login)

    implicit val userUpdateController: UserUpdateController[F] =
      UserUpdateController.make[F](services.userUpdate)

    implicit val userFavoriteArtistsUpdateController: UserFavoriteArtistsUpdateController[
      F
    ] =
      UserFavoriteArtistsUpdateController.make[F](
        services.userFavoriteArtistsUpdate
      )

    implicit val userFavoriteGenresUpdateController: UserFavoriteGenresUpdateController[
      F
    ] =
      UserFavoriteGenresUpdateController.make[F](
        services.userFavoriteGenresUpdate
      )

    implicit val userPurposesUpdateController: UserPurposesUpdateController[F] =
      UserPurposesUpdateController.make[F](services.userPurposesUpdate)

    implicit val userPotentialFriendsGetController: UserPotentialFriendsGetController[
      F
    ] =
      UserPotentialFriendsGetController.make[F](
        services.userPotentialFriendsGet
      )

    implicit val artistsGetController: ArtistsGetController[F] =
      ArtistsGetController.make[F](services.artistsGet)

    implicit val userPurposesGetController: UserPurposesGetController[F] =
      UserPurposesGetController.make[F](services.userPurposesGet)

    implicit val genresGetController: GenresGetController[F] =
      GenresGetController.make[F](services.genresGet)

    implicit val citiesGetController: CitiesGetController[F] =
      CitiesGetController.make[F](services.citiesGet)

    implicit val chatCreateController: ChatCreateController[F] =
      ChatCreateController.make[F](services.chatCreate)

    implicit val chatsGetController: UserChatsGetController[F] =
      UserChatsGetController.make[F](services.chatsGet)

    new PublicControllers[F]
  }
}
