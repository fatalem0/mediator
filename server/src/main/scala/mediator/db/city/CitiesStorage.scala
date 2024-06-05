package mediator.db.city

import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import doobie.implicits._
import mediator.Domain.City

@derive(applyK)
private[db] trait CitiesStorage[F[_]] {
  def getCities: F[Vector[City]]
  def getByID(cityID: City.ID): F[Option[City]]
}

object CitiesStorage {
  final private object DB extends CitiesStorage[ConnectionIO] {
    override def getCities: ConnectionIO[Vector[City]] =
      sql"""
          SELECT
            id,
            name
          FROM cities
         """.query[City].to[Vector]

    override def getByID(cityID: City.ID): ConnectionIO[Option[City]] =
      sql"""
           SELECT
             id,
             name
           FROM
             cities
           WHERE
             id = $cityID
         """.query[City].option
  }

  def db: CitiesStorage[ConnectionIO] = DB
}
