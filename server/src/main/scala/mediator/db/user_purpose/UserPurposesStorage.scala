package mediator.db.user_purpose

import cats.data.NonEmptyVector
import derevo.derive
import derevo.tagless.applyK
import doobie.{ ConnectionIO, Fragments, Update }
import doobie.implicits._
import mediator.Domain.{ User, UserPurpose }

@derive(applyK)
private[db] trait UserPurposesStorage[F[_]] {
  def get: F[Vector[UserPurpose]]
  def getByUserID(userID: User.ID): F[Vector[UserPurpose]]

  def update(
      userId: User.ID,
      userPurposeIds: NonEmptyVector[UserPurpose.ID]
  ): F[Boolean]
}

object UserPurposesStorage {
  final private object DB extends UserPurposesStorage[ConnectionIO] {
    override def get: ConnectionIO[Vector[UserPurpose]] =
      sql"""
          SELECT
            id,
            name
          FROM user_purposes
         """.query[UserPurpose].to[Vector]

    override def getByUserID(
        userID: User.ID
    ): ConnectionIO[Vector[UserPurpose]] =
      sql"""
           SELECT
             up.id,
             up.name
           FROM
             user_purposes_users upu
           JOIN
             user_purposes up ON upu.user_purpose_id = up.id
           WHERE
             upu.user_id = $userID
         """.query[UserPurpose].to[Vector]

    override def update(
        userId: User.ID,
        userPurposeIds: NonEmptyVector[UserPurpose.ID]
    ): ConnectionIO[Boolean] = {
      val delete =
        sql"""
             DELETE FROM user_purposes_users
             WHERE user_id = $userId AND ${Fragments.notIn(
            fr"user_purpose_id",
            userPurposeIds
          )}
           """

      val insert = """
          INSERT INTO user_purposes_users
          VALUES (?, ?)
          ON CONFLICT DO NOTHING
        """

      for {
        deletedRowsCount <- delete.update.run
        insertedRowsCount <- Update[(UserPurpose.ID, User.ID)](
          insert
        ).updateMany(userPurposeIds.map((_, userId)))
      } yield deletedRowsCount > 0 || insertedRowsCount > 0
    }
  }

  def db: UserPurposesStorage[ConnectionIO] = DB
}
