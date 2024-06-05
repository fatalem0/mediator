package wiring

import cats.effect.{ Async, Clock }
import mediator.artist.ArtistsGetService
import mediator.chat.ChatCreateService
import mediator.city.CitiesGetService
import mediator.favorite_artist.UserFavoriteArtistsUpdateService
import mediator.favorite_genre.UserFavoriteGenresUpdateService
import mediator.genre.GenresGetService
import mediator.login.LoginService
import mediator.potential_friend.PotentialFriendsGetService
import mediator.registration.RegistrationService
import mediator.token.TokenService
import mediator.user.create.UserCreateService
import mediator.user.update.UserUpdateService
import mediator.user_chat.UserChatsGetService
import mediator.user_purpose.get.UserPurposesGetService
import mediator.user_purpose.update.UserPurposesUpdateService
import tofu.generate.GenUUID

class ServiceComponent[F[_]](implicit
    val auth: TokenService[F],
    val userCreate: UserCreateService[F],
    val userUpdate: UserUpdateService[F],
    val userFavoriteArtistsUpdate: UserFavoriteArtistsUpdateService[F],
    val userFavoriteGenresUpdate: UserFavoriteGenresUpdateService[F],
    val userPurposesUpdate: UserPurposesUpdateService[F],
    val userPotentialFriendsGet: PotentialFriendsGetService[F],
    val registration: RegistrationService[F],
    val login: LoginService[F],
    val artistsGet: ArtistsGetService[F],
    val userPurposesGet: UserPurposesGetService[F],
    val genresGet: GenresGetService[F],
    val citiesGet: CitiesGetService[F],
    val chatCreate: ChatCreateService[F],
    val chatsGet: UserChatsGetService[F]
)

object ServiceComponent {
  def make[I[_]: Async: Clock: GenUUID](
      core: CoreComponent[I],
      db: DatabaseComponent[I]
  ): ServiceComponent[I] = {
    import core._

    implicit val tokenService: TokenService[I] =
      TokenService.make[I](conf.auth.ttl)

    implicit val userCreate: UserCreateService[I] = UserCreateService.make[I](
      db.userCreateStorage,
      db.userCheckStorage
    )

    implicit val userUpdate: UserUpdateService[I] =
      UserUpdateService.makeObservable[I](db.userUpdateStorage)

    implicit val userFavoriteArtistsUpdate: UserFavoriteArtistsUpdateService[
      I
    ] =
      UserFavoriteArtistsUpdateService.makeObservable[I](
        db.favoriteArtistsUsersUpdateStorage
      )

    implicit val userFavoriteGenresUpdate: UserFavoriteGenresUpdateService[I] =
      UserFavoriteGenresUpdateService.makeObservable[I](
        db.favoriteGenresUsersUpdateStorage
      )

    implicit val userPurposesUpdate: UserPurposesUpdateService[I] =
      UserPurposesUpdateService.makeObservable[I](
        db.userPurposesUsersUpdateStorage
      )

    implicit val userPotentialFriendsGet: PotentialFriendsGetService[I] =
      PotentialFriendsGetService.make[I](
        db.userGetStorage,
        db.potentialFriendsUpdateStorage,
        db.favoriteArtistsUsersGetStorage,
        db.favoriteGenresUsersGetStorage,
        db.userPurposesGetStorage,
        db.citiesGetStorage
      )

    implicit val registration: RegistrationService[I] =
      RegistrationService.makeObservable[I](tokenService, userCreate)

    implicit val login: LoginService[I] = LoginService.makeObservable[I](
      db.userGetStorage,
      tokenService
    )

    implicit val artistsGet: ArtistsGetService[I] =
      ArtistsGetService.make[I](db.artistsGetStorage)

    implicit val userPurposesGet: UserPurposesGetService[I] =
      UserPurposesGetService.make[I](db.userPurposesGetStorage)

    implicit val genresGet: GenresGetService[I] =
      GenresGetService.make[I](db.genresGetStorage)

    implicit val citiesGet: CitiesGetService[I] =
      CitiesGetService.make[I](db.citiesGetStorage)

    implicit val chatCreate: ChatCreateService[I] = ChatCreateService.make[I](
      db.chatCreateStorage,
      db.chatCheckStorage
    )

    implicit val chatsGet: UserChatsGetService[I] = UserChatsGetService.make[I](
      db.chatGetStorage
    )

    new ServiceComponent[I]
  }
}
