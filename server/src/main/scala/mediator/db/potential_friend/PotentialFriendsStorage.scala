package mediator.db.potential_friend

import cats.data.NonEmptyVector
import derevo.derive
import derevo.tagless.applyK
import doobie.implicits._
import doobie.{ ConnectionIO, Fragments, Update }
import mediator.Domain.User
import mediator.db.user.get.Domain.UserWithMatchingPercent
import mediator.potential_friend.Domain.MatchingPercent

@derive(applyK)
private[db] trait PotentialFriendsStorage[F[_]] {
  def update(
      userID: User.ID,
      matchedUsers: NonEmptyVector[UserWithMatchingPercent]
  ): F[Boolean]
}

object PotentialFriendsStorage {
  private object DB extends PotentialFriendsStorage[ConnectionIO] {
    override def update(
        userID: User.ID,
        matchedUsers: NonEmptyVector[UserWithMatchingPercent]
    ): ConnectionIO[Boolean] = {
      val delete =
        sql"""
             DELETE FROM potential_interlocutors_users
             WHERE user_id = $userID AND ${Fragments.notIn(
            fr"potential_interlocutor_id",
            matchedUsers.map(_.id)
          )}
           """

      val insert = """
          INSERT INTO potential_interlocutors_users
          VALUES (?, ?, ?)
          ON CONFLICT DO NOTHING
        """

      for {
        deletedRowsCount <- delete.update.run

        insertedRowsCount <- Update[(User.ID, User.ID, MatchingPercent)](insert)
          .updateMany(matchedUsers.map(matchedUser =>
            (matchedUser.id, userID, matchedUser.matchingPercent)
          ))
      } yield deletedRowsCount > 0 || insertedRowsCount > 0
    }
  }

  def db: PotentialFriendsStorage[ConnectionIO] = DB
}
