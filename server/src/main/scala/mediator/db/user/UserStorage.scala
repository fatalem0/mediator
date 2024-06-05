package mediator.db.user

import cats.data.NonEmptyVector
import derevo.derive
import derevo.tagless.applyK
import doobie.{ ConnectionIO, Fragments }
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.fragment.Fragment
import mediator.Domain.{ Artist, Genre, Limit, User, UserPurpose }
import mediator.db.user.create.Domain.CreateUserRow
import mediator.db.user.get.Domain.UserWithMatchingPercent
import mediator.db.user.update.Domain.UpdateUserRow
import mediator.potential_friend.Domain.MatchingPercent

@derive(applyK)
private[db] trait UserStorage[F[_]] {
  def create(req: CreateUserRow): F[Boolean]
  def getByEmail(email: User.Email): F[Option[User]]
  def getByID(userID: User.ID): F[Option[User]]

  def getUserIDsWithSimilarInterests(
      userID: User.ID,
      favoriteArtistIDs: NonEmptyVector[Artist.ID],
      favoriteGenreIDs: NonEmptyVector[Genre.ID],
      userPurposeIDs: NonEmptyVector[UserPurpose.ID],
      limit: Limit,
      matchingPercent: MatchingPercent
  ): F[Vector[UserWithMatchingPercent]]

  def update(
      userId: User.ID,
      updateUserRow: UpdateUserRow
  ): F[Boolean]
}

object UserStorage {
  final private object DB extends UserStorage[ConnectionIO] {
    override def create(row: CreateUserRow): ConnectionIO[Boolean] =
      sql"""
        INSERT INTO users(
          id,
          email,
          hashed_password,
          created_at,
          updated_at
        ) VALUES (
          ${row.id},
          ${row.email},
          ${row.hashedPassword},
          ${row.createdAt},
          ${row.updatedAt}
        )
      """.update.run.map(_ > 0)

    override def getByEmail(email: User.Email): ConnectionIO[Option[User]] =
      sql"""
           SELECT
             id,
             email,
             hashed_password,
             created_at,
             updated_at,
             account_name,
             image_url,
             about,
             city
           FROM
             users
           WHERE
             email = $email
         """.query[User].option

    override def getByID(userID: User.ID): ConnectionIO[Option[User]] =
      sql"""
           SELECT
             id,
             email,
             hashed_password,
             created_at,
             updated_at,
             account_name,
             image_url,
             about,
             city
           FROM
             users
           WHERE
             id = $userID
         """.query[User].option

    override def getUserIDsWithSimilarInterests(
        userID: User.ID,
        favoriteArtistIDs: NonEmptyVector[Artist.ID],
        favoriteGenreIDs: NonEmptyVector[Genre.ID],
        userPurposeIDs: NonEmptyVector[UserPurpose.ID],
        limit: Limit,
        matchingPercent: MatchingPercent
    ): ConnectionIO[Vector[UserWithMatchingPercent]] = {
      val userInterestsLength =
        favoriteArtistIDs.length + favoriteGenreIDs.length + userPurposeIDs.length

      val favoriteArtistInFragment = Fragments.in(
        fr"favorite_artist_id",
        favoriteArtistIDs
      )
      val favoriteGenreInFragment = Fragments.in(
        fr"favorite_genre_id",
        favoriteGenreIDs
      )
      val userPurposeInFragment = Fragments.in(
        fr"user_purpose_id",
        userPurposeIDs
      )

      val selectFromFavoriteArtistsFragment =
        fr"""
            SELECT user_id
            FROM favorite_artists_users
            WHERE user_id != $userID
            AND $favoriteArtistInFragment
          """

      val selectFromFavoriteGenresFragment =
        fr"""
            SELECT user_id
            FROM favorite_genres_users
            WHERE user_id != $userID
            AND $favoriteGenreInFragment
          """

      val selectFromUserPurposesFragment =
        fr"""
            SELECT user_id
            FROM user_purposes_users
            WHERE user_id != $userID
            AND $userPurposeInFragment
          """

      val unionInterestsFragment =
        fr"""
            $selectFromFavoriteArtistsFragment
            UNION ALL
            $selectFromFavoriteGenresFragment
            UNION ALL
            $selectFromUserPurposesFragment
          """

      val matchPercentFragment =
        fr"ROUND(COUNT(*) * (100.0 / $userInterestsLength), 2)"

      val getUserIDsFragment =
        fr"""
            SELECT user_id, $matchPercentFragment AS match_percent
            FROM ($unionInterestsFragment) AS interests
            GROUP BY user_id
            HAVING $matchPercentFragment > $matchingPercent
            LIMIT $limit
          """

      getUserIDsFragment.query[UserWithMatchingPercent].to[Vector]
    }

    override def update(
        userId: User.ID,
        updateUserRow: UpdateUserRow
    ): ConnectionIO[Boolean] = {
      val setList: List[Option[Fragment]] = List(
        updateUserRow.email.map(email => fr"email = $email"),
        updateUserRow.hashedPassword.map(password =>
          fr"hashed_password = $password"
        ),
        Some(fr"updated_at = ${updateUserRow.updatedAt}"),
        updateUserRow.accountName.map(accountName =>
          fr"account_name = $accountName"
        ),
        updateUserRow.imageURL.map(imageURL => fr"image_url = $imageURL"),
        updateUserRow.about.map(about => fr"about = $about"),
        updateUserRow.city.map(city => fr"city = $city")
      )

      val updateStatement: List[Option[Fragment]] => Fragment =
        setList =>
          sql"""
               UPDATE
                 users
               ${Fragments.setOpt(setList: _*)}
               WHERE
                 id = $userId
             """

      updateStatement(setList).update.run.map(_ > 0)
    }
  }

  def db: UserStorage[ConnectionIO] = DB
}
