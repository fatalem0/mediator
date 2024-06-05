package mediator.db.genre

import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import doobie.implicits._
import mediator.Domain.{ Artist, Genre }

@derive(applyK)
private[db] trait GenresStorage[F[_]] {
  def getGenres: F[Vector[Genre]]
  def getById(genreId: Genre.ID): F[Option[Genre]]
  def getByArtistID(artistId: Artist.ID): F[Option[Genre.ID]]
}

object GenresStorage {
  final private object DB extends GenresStorage[ConnectionIO] {
    override def getGenres: ConnectionIO[Vector[Genre]] =
      sql"""
          SELECT
            id,
            name,
            image_url
          FROM genres
         """.query[Genre].to[Vector]

    override def getById(genreId: Genre.ID): ConnectionIO[Option[Genre]] =
      sql"""
          SELECT
            id,
            name,
            image_url
          FROM genres
          WHERE id = $genreId
         """.query[Genre].option

    override def getByArtistID(
        artistId: Artist.ID
    ): ConnectionIO[Option[Genre.ID]] =
      sql"""
          SELECT
            genre_id
          FROM
            genres_artists
          WHERE
            artist_id = $artistId
         """.query[Genre.ID].option
  }

  def db: GenresStorage[ConnectionIO] = DB
}
