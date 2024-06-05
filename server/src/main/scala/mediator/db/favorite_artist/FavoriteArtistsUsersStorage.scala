package mediator.db.favorite_artist

import cats.data.NonEmptyVector
import derevo.derive
import derevo.tagless.applyK
import doobie.implicits._
import doobie.{ ConnectionIO, Fragments, Update }
import mediator.Domain.{ Artist, User }

@derive(applyK)
private[db] trait FavoriteArtistsUsersStorage[F[_]] {
  def getArtistIDsByUserID(
      userId: User.ID,
      isEqualTo: Boolean
  ): F[Vector[Artist.ID]]

  def getUserFavoriteArtists(userID: User.ID): F[Vector[Artist]]

  def update(
      userId: User.ID,
      favoriteArtistIds: NonEmptyVector[Artist.ID]
  ): F[Boolean]
}

object FavoriteArtistsUsersStorage {
  private object DB extends FavoriteArtistsUsersStorage[ConnectionIO] {
    override def getArtistIDsByUserID(
        userId: User.ID,
        isEqualTo: Boolean
    ): ConnectionIO[Vector[Artist.ID]] =
      sql"""
          SELECT
            favorite_artist_id
          WHERE
            user_id = $userId
          FROM favorite_artists_users
         """.query[Artist.ID].to[Vector]

    override def getUserFavoriteArtists(
        userID: User.ID
    ): ConnectionIO[Vector[Artist]] =
      sql"""
           SELECT
             a.id,
             a.name,
             a.image_url
           FROM
             favorite_artists_users fau
           JOIN
             artists a ON fau.favorite_artist_id = a.id
           WHERE
             fau.user_id = $userID
         """.query[Artist].to[Vector]

    override def update(
        userId: User.ID,
        favoriteArtistIds: NonEmptyVector[Artist.ID]
    ): ConnectionIO[Boolean] = {
      val delete =
        sql"""
             DELETE FROM
               favorite_artists_users
             WHERE
               user_id = $userId AND ${Fragments.notIn(
            fr"favorite_artist_id",
            favoriteArtistIds
          )}
           """

      val insert = """
          INSERT INTO
            favorite_artists_users
          VALUES
            (?, ?)
          ON CONFLICT DO NOTHING
        """

      for {
        deletedRowsCount <- delete.update.run
        insertedRowsCount <- Update[(Artist.ID, User.ID)](insert).updateMany(
          favoriteArtistIds.map((_, userId))
        )
      } yield deletedRowsCount > 0 || insertedRowsCount > 0
    }
  }

  def db: FavoriteArtistsUsersStorage[ConnectionIO] = DB
}
