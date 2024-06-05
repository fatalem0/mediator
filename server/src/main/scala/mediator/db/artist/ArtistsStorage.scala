package mediator.db.artist

import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import doobie.implicits._
import mediator.Domain.{ Artist, Limit, Offset }

@derive(applyK)
private[db] trait ArtistsStorage[F[_]] {
  def get(
      limit: Limit,
      offset: Offset
  ): F[Vector[Artist]]
}

object ArtistsStorage {
  final private object DB extends ArtistsStorage[ConnectionIO] {
    override def get(
        limit: Limit,
        offset: Offset
    ): ConnectionIO[Vector[Artist]] =
      sql"""
          SELECT
            id,
            name,
            image_url
          FROM artists
          LIMIT ${limit.value} OFFSET ${offset.value}
         """.query[Artist].to[Vector]
  }

  def db: ArtistsStorage[ConnectionIO] = DB
}
