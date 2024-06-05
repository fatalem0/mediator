package mediator.db.favorite_genre

import cats.data.NonEmptyVector
import derevo.derive
import derevo.tagless.applyK
import doobie.implicits._
import doobie.{ ConnectionIO, Fragments, Update }
import mediator.Domain.{ Genre, User }

@derive(applyK)
private[db] trait FavoriteGenresUsersStorage[F[_]] {
  def getGenreIDsByUserID(userId: User.ID): F[Vector[Genre.ID]]
  def getUserFavoriteGenres(userID: User.ID): F[Vector[Genre]]

  def update(
      userId: User.ID,
      favoriteGenreIds: NonEmptyVector[Genre.ID]
  ): F[Boolean]
}

object FavoriteGenresUsersStorage {
  private object DB extends FavoriteGenresUsersStorage[ConnectionIO] {
    override def getGenreIDsByUserID(
        userId: User.ID
    ): ConnectionIO[Vector[Genre.ID]] =
      sql"""
          SELECT
            favorite_genre_id
          WHERE
            user_id = $userId
          FROM favorite_genres_users
         """.query[Genre.ID].to[Vector]

    override def getUserFavoriteGenres(
        userID: User.ID
    ): ConnectionIO[Vector[Genre]] =
      sql"""
           SELECT
             g.id,
             g.name,
             g.image_url
           FROM
             favorite_genres_users fgu
           JOIN
             genres g ON fgu.favorite_genre_id = g.id
           WHERE
             fgu.user_id = $userID
         """.query[Genre].to[Vector]

    override def update(
        userId: User.ID,
        favoriteGenreIds: NonEmptyVector[Genre.ID]
    ): ConnectionIO[Boolean] = {
      val delete =
        sql"""
             DELETE FROM favorite_genres_users
             WHERE user_id = $userId AND ${Fragments.notIn(
            fr"favorite_genre_id",
            favoriteGenreIds
          )}
           """

      val insert = """
          INSERT INTO favorite_genres_users
          VALUES (?, ?)
          ON CONFLICT DO NOTHING
        """

      for {
        deletedRowsCount <- delete.update.run
        insertedRowsCount <- Update[(Genre.ID, User.ID)](insert).updateMany(
          favoriteGenreIds.map((
            _,
            userId
          ))
        )
      } yield deletedRowsCount > 0 || insertedRowsCount > 0
    }
  }

  def db: FavoriteGenresUsersStorage[ConnectionIO] = DB
}
