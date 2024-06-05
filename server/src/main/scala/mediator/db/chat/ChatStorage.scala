package mediator.db.chat

import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import mediator.Domain.{ Limit, Offset, User }
import mediator.chat.Domain.Chat
import mediator.db.chat.get.Domain.ChatRow

@derive(applyK)
private[db] trait ChatStorage[F[_]] {
  def create(chatRow: ChatRow): F[Boolean]

  def get(
      initiatorID: User.ID,
      friendID: User.ID
  ): F[Option[Chat]]

  def getByUserId(
      limit: Limit,
      offset: Offset,
      userId: User.ID
  ): F[Vector[ChatRow]]
}

object ChatStorage {
  final private object DB extends ChatStorage[ConnectionIO] {
    override def create(chatRow: ChatRow): ConnectionIO[Boolean] =
      sql"""
        INSERT INTO chats(
          id,
          initiator_id,
          interlocutor_id,
          created_at,
          updated_at
        ) VALUES (
          ${chatRow.id},
          ${chatRow.initiatorId},
          ${chatRow.friendId},
          ${chatRow.createdAt},
          ${chatRow.updatedAt}
        )
      """.update.run.map(_ > 0)

    override def get(
        initiatorID: User.ID,
        friendID: User.ID
    ): ConnectionIO[Option[Chat]] =
      sql"""
           SELECT
             id,
             initiator_id,
             interlocutor_id,
             created_at,
             updated_at,
             last_sent_message,
             last_time_message_sent
           FROM
             chats
           WHERE
             initiator_id = $initiatorID
           AND
             interlocutor_id = $friendID
         """.query[Chat].option

    override def getByUserId(
        limit: Limit,
        offset: Offset,
        userId: User.ID
    ): ConnectionIO[Vector[ChatRow]] =
      sql"""
          SELECT
            id,
            initiator_id,
            interlocutor_id,
            created_at,
            updated_at,
            last_sent_message,
            last_time_message_sent
          FROM
            chats
          WHERE
            (initiator_id = $userId AND interlocutor_id <> $userId)
          OR
            (initiator_id <> $userId AND interlocutor_id = $userId)
          LIMIT ${limit.value} OFFSET ${offset.value}
         """.query[ChatRow].to[Vector]
  }

  def db: ChatStorage[ConnectionIO] = DB
}
