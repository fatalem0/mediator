package wiring

import cats.effect.{ Async, Resource }
import doobie.Transactor
import mediator.db.artist.ArtistsGetStorage
import mediator.db.chat.check.ChatCheckStorage
import mediator.db.chat.create.ChatCreateStorage
import mediator.db.chat.get.ChatGetStorage
import mediator.db.city.CitiesGetStorage
import mediator.db.favorite_artist.get.FavoriteArtistsUsersGetStorage
import mediator.db.favorite_artist.update.FavoriteArtistsUsersUpdateStorage
import mediator.db.favorite_genre.get.FavoriteGenresUsersGetStorage
import mediator.db.favorite_genre.update.FavoriteGenresUsersUpdateStorage
import mediator.db.genre.GenresGetStorage
import mediator.db.potential_friend.PotentialFriendsUpdateStorage
import mediator.db.user.check.UserCheckStorage
import mediator.db.user.create.UserCreateStorage
import mediator.db.user.get.UserGetStorage
import mediator.db.user.update.UserUpdateStorage
import mediator.db.user_purpose.get.UserPurposesGetStorage
import mediator.db.user_purpose.update.UserPurposesUpdateStorage
import utils.db.{ DatabaseTransactor, SafeTransactor }

class DatabaseComponent[F[_]](implicit
    val transactor: Transactor[F],
    val safeTransactor: SafeTransactor[F],
    val userCreateStorage: UserCreateStorage[F],
    val userGetStorage: UserGetStorage[F],
    val userCheckStorage: UserCheckStorage[F],
    val userUpdateStorage: UserUpdateStorage[F],
    val potentialFriendsUpdateStorage: PotentialFriendsUpdateStorage[F],
    val favoriteArtistsUsersGetStorage: FavoriteArtistsUsersGetStorage[F],
    val favoriteArtistsUsersUpdateStorage: FavoriteArtistsUsersUpdateStorage[F],
    val favoriteGenresUsersGetStorage: FavoriteGenresUsersGetStorage[F],
    val favoriteGenresUsersUpdateStorage: FavoriteGenresUsersUpdateStorage[F],
    val userPurposesUsersUpdateStorage: UserPurposesUpdateStorage[F],
    val artistsGetStorage: ArtistsGetStorage[F],
    val userPurposesGetStorage: UserPurposesGetStorage[F],
    val genresGetStorage: GenresGetStorage[F],
    val citiesGetStorage: CitiesGetStorage[F],
    val chatCreateStorage: ChatCreateStorage[F],
    val chatGetStorage: ChatGetStorage[F],
    val chatCheckStorage: ChatCheckStorage[F]
)

object DatabaseComponent {
  def make[I[_]: Async](
      core: CoreComponent[I]
  ): Resource[I, DatabaseComponent[I]] = {
    import core._

    for {
      implicit0(transactor: Transactor[I]) <- DatabaseTransactor.make[I](
        conf.database
      )

      implicit0(safeTransactor: SafeTransactor[I]) = SafeTransactor.make[I](
        transactor
      )

      implicit0(userCreateStorage: UserCreateStorage[I]) =
        UserCreateStorage.makeObservable[I]
      implicit0(userGetStorage: UserGetStorage[I]) =
        UserGetStorage.makeObservable[I]
      implicit0(userCheckStorage: UserCheckStorage[I]) =
        UserCheckStorage.makeObservable[I]
      implicit0(userUpdateStorage: UserUpdateStorage[I]) =
        UserUpdateStorage.makeObservable[I]

      implicit0(
        potentialFriendsUpdateStorage: PotentialFriendsUpdateStorage[I]
      ) = PotentialFriendsUpdateStorage.makeObservable[I]

      implicit0(
        favoriteArtistsUsersGetStorage: FavoriteArtistsUsersGetStorage[I]
      ) = FavoriteArtistsUsersGetStorage.makeObservable[I]
      implicit0(
        favoriteArtistsUsersUpdateStorage: FavoriteArtistsUsersUpdateStorage[I]
      ) = FavoriteArtistsUsersUpdateStorage.makeObservable[I]

      implicit0(
        favoriteGenresUsersGetStorage: FavoriteGenresUsersGetStorage[I]
      ) = FavoriteGenresUsersGetStorage.makeObservable[I]
      implicit0(
        favoriteGenresUsersUpdateStorage: FavoriteGenresUsersUpdateStorage[I]
      ) = FavoriteGenresUsersUpdateStorage.makeObservable[I]

      implicit0(userPurposesUsersUpdateStorage: UserPurposesUpdateStorage[I]) =
        UserPurposesUpdateStorage.makeObservable[I]

      implicit0(artistsGetStorage: ArtistsGetStorage[I]) =
        ArtistsGetStorage.makeObservable[I]
      implicit0(userPurposesGetStorage: UserPurposesGetStorage[I]) =
        UserPurposesGetStorage.makeObservable[I]
      implicit0(genresGetStorage: GenresGetStorage[I]) =
        GenresGetStorage.makeObservable[I]
      implicit0(citiesGetStorage: CitiesGetStorage[I]) =
        CitiesGetStorage.makeObservable[I]

      implicit0(chatCreateStorage: ChatCreateStorage[I]) =
        ChatCreateStorage.makeObservable[I]
      implicit0(chatGetStorage: ChatGetStorage[I]) =
        ChatGetStorage.makeObservable[I]
      implicit0(chatCheckStorage: ChatCheckStorage[I]) =
        ChatCheckStorage.makeObservable[I]

      comp = new DatabaseComponent[I]
    } yield comp
  }
}
