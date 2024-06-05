package mediator.potential_friend

import cats.Monad
import cats.data.NonEmptyVector
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import cats.syntax.vector._
import derevo.derive
import derevo.tagless.applyK
import io.scalaland.chimney.dsl._
import mediator.Domain._
import mediator.db.city.CitiesGetStorage
import mediator.db.city.Domain.Errors.{ GetError => CityGetError }
import mediator.db.favorite_artist.get.Domain.Errors.{ GetError => FavoriteArtistsGetError }
import mediator.db.favorite_artist.get.FavoriteArtistsUsersGetStorage
import mediator.db.favorite_genre.get.Domain.Errors.{ GetError => FavoriteGenresGetError }
import mediator.db.favorite_genre.get.FavoriteGenresUsersGetStorage
import mediator.db.potential_friend.PotentialFriendsUpdateStorage
import mediator.db.user.get.Domain.Errors.{ GetError => UserGetError }
import mediator.db.user.get.Domain.UserWithMatchingPercent
import mediator.db.user.get.UserGetStorage
import mediator.db.user_purpose.get.Domain.Errors.{ GetError => UserPurposesGetError }
import mediator.db.user_purpose.get.UserPurposesGetStorage
import mediator.potential_friend.Domain.Errors.UserPotentialFriendsGetError
import mediator.potential_friend.Domain.{ GetPotentialFriends, MatchingPercent, PotentialFriend }
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.raise._

@derive(applyK)
trait PotentialFriendsGetService[F[_]] {
  def getUserPotentialFriends(
      userID: User.ID,
      limit: Limit,
      offset: Offset
  ): F[Either[UserPotentialFriendsGetError, GetPotentialFriends.Response]]
}

object PotentialFriendsGetService {
  final private class Impl[F[_]: Monad: UserPotentialFriendsGetError.Errors](
      userGetStorage: UserGetStorage[F],
      userPotentialFriendsUpdateStorage: PotentialFriendsUpdateStorage[F],
      favoriteArtistsUsersGetStorage: FavoriteArtistsUsersGetStorage[F],
      favoriteGenresUsersGetStorage: FavoriteGenresUsersGetStorage[F],
      userPurposesGetStorage: UserPurposesGetStorage[F],
      citiesGetStorage: CitiesGetStorage[F]
  ) extends PotentialFriendsGetService[F] {
    override def getUserPotentialFriends(
        userID: User.ID,
        limit: Limit,
        offset: Offset
    ): F[Either[UserPotentialFriendsGetError, GetPotentialFriends.Response]] =
      (for {
        favoriteArtistIDs <- getUserFavoriteArtists(userID).flatMap(
          _.map(_.id).toNev.orRaise[F](UserPotentialFriendsGetError.NotFound)
        )

        favoriteGenreIDs <- getUserFavoriteGenres(userID).flatMap(
          _.map(_.id).toNev.orRaise[F](UserPotentialFriendsGetError.NotFound)
        )

        userPurposeIDs <- getUserPurposes(userID).flatMap(
          _.map(_.id).toNev.orRaise[F](UserPotentialFriendsGetError.NotFound)
        )

        matchedUsers <-
          userGetStorage.getMatchedUsers(
            userID,
            favoriteArtistIDs,
            favoriteGenreIDs,
            userPurposeIDs,
            limit,
            MatchingPercent(50.00)
          )
            .leftMapIn[UserPotentialFriendsGetError] {
              case UserGetError.NotFound =>
                UserPotentialFriendsGetError.NotFound
              case UserGetError.PSQL(cause) =>
                UserPotentialFriendsGetError.InternalDatabase(cause)
              case UserGetError.Connection(cause) =>
                UserPotentialFriendsGetError.Internal(cause)
            }
            .reRaise

        _ <- userPotentialFriendsUpdateStorage.update(
          userID,
          NonEmptyVector.fromVectorUnsafe(matchedUsers)
        )

        potentialFriends <- matchedUsers
          .traverse(transformIntoPotentialFriend)
          .map(potentialFriends =>
            GetPotentialFriends.Response(
              offset,
              potentialFriends.drop(offset.value)
            )
          )
      } yield potentialFriends).attempt[UserPotentialFriendsGetError]

    private def transformIntoPotentialFriend(
        userWithMatchingPercent: UserWithMatchingPercent
    ): F[PotentialFriend] =
      for {
        user <- getUser(userWithMatchingPercent.id)

        userCityID <- user.cityID.orRaise[F](
          UserPotentialFriendsGetError.NotFound
        )

        userAccountName <- user.accountName.orRaise[F](
          UserPotentialFriendsGetError.NotFound
        )

        userAbout <- user.about.orRaise[F](
          UserPotentialFriendsGetError.NotFound
        )

        favoriteArtistNames <- getUserFavoriteArtists(user.id)
        favoriteGenreNames  <- getUserFavoriteGenres(user.id)
        userPurposeNames    <- getUserPurposes(user.id)
        userCityName        <- getUserCityName(userCityID)
      } yield user
        .into[PotentialFriend]
        .withFieldConst(_.accountName, userAccountName)
        .withFieldConst(_.imageURL, user.imageURL)
        .withFieldConst(_.about, userAbout)
        .withFieldConst(_.city, userCityName)
        .withFieldConst(_.userPurposes, userPurposeNames.map(_.name))
        .withFieldConst(_.favoriteGenres, favoriteGenreNames.map(_.name))
        .withFieldConst(_.favoriteArtists, favoriteArtistNames.map(_.name))
        .withFieldConst(
          _.matchingPercent,
          userWithMatchingPercent.matchingPercent
        )
        .transform

    private def getUser(userID: User.ID): F[User] =
      userGetStorage
        .getByID(userID)
        .leftMapIn[UserPotentialFriendsGetError] {
          case UserGetError.NotFound => UserPotentialFriendsGetError.NotFound
          case UserGetError.PSQL(cause) =>
            UserPotentialFriendsGetError.InternalDatabase(cause)
          case UserGetError.Connection(cause) =>
            UserPotentialFriendsGetError.Internal(cause)
        }
        .reRaise

    private def getUserFavoriteArtists(userID: User.ID): F[Vector[Artist]] =
      favoriteArtistsUsersGetStorage
        .get(userID)
        .leftMapIn[UserPotentialFriendsGetError] {
          case FavoriteArtistsGetError.NotFound =>
            UserPotentialFriendsGetError.NotFound
          case FavoriteArtistsGetError.PSQL(cause) =>
            UserPotentialFriendsGetError.InternalDatabase(cause)
          case FavoriteArtistsGetError.Connection(cause) =>
            UserPotentialFriendsGetError.Internal(cause)
        }
        .reRaise

    private def getUserFavoriteGenres(userID: User.ID): F[Vector[Genre]] =
      favoriteGenresUsersGetStorage
        .get(userID)
        .leftMapIn[UserPotentialFriendsGetError] {
          case FavoriteGenresGetError.NotFound =>
            UserPotentialFriendsGetError.NotFound
          case FavoriteGenresGetError.PSQL(cause) =>
            UserPotentialFriendsGetError.InternalDatabase(cause)
          case FavoriteGenresGetError.Connection(cause) =>
            UserPotentialFriendsGetError.Internal(cause)
        }
        .reRaise

    private def getUserPurposes(userID: User.ID): F[Vector[UserPurpose]] =
      userPurposesGetStorage
        .getByUserID(userID)
        .leftMapIn[UserPotentialFriendsGetError] {
          case UserPurposesGetError.NotFound =>
            UserPotentialFriendsGetError.NotFound
          case UserPurposesGetError.PSQL(cause) =>
            UserPotentialFriendsGetError.InternalDatabase(cause)
          case UserPurposesGetError.Connection(cause) =>
            UserPotentialFriendsGetError.Internal(cause)
        }
        .reRaise

    private def getUserCityName(cityID: City.ID): F[City.Name] =
      citiesGetStorage
        .getByID(cityID)
        .mapIn(_.name)
        .leftMapIn {
          case CityGetError.NotFound => UserPotentialFriendsGetError.NotFound
          case CityGetError.PSQL(cause) =>
            UserPotentialFriendsGetError.InternalDatabase(cause)
          case CityGetError.Connection(cause) =>
            UserPotentialFriendsGetError.Internal(cause)
        }
        .reRaise
  }

  def make[F[_]: Monad: UserPotentialFriendsGetError.Errors](
      userGetStorage: UserGetStorage[F],
      userPotentialFriendsUpdateStorage: PotentialFriendsUpdateStorage[F],
      favoriteArtistsUsersGetStorage: FavoriteArtistsUsersGetStorage[F],
      favoriteGenresUsersGetStorage: FavoriteGenresUsersGetStorage[F],
      userPurposesGetStorage: UserPurposesGetStorage[F],
      citiesGetStorage: CitiesGetStorage[F]
  ): PotentialFriendsGetService[F] =
    new Impl[F](
      userGetStorage,
      userPotentialFriendsUpdateStorage,
      favoriteArtistsUsersGetStorage,
      favoriteGenresUsersGetStorage,
      userPurposesGetStorage,
      citiesGetStorage
    )
}
