package mediator.db.chat.get

import mediator.Domain.User
import mediator.chat.Domain.{ Chat, Message }
import org.postgresql.util.PSQLException
import utils.db.SQLErrorJoiner
import utils.db.model.DatabaseError

import java.time.Instant
import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed trait GetError

    object GetError {
      final case class UserChatsNotFound(userId: User.ID)              extends GetError
      final case class UserFriendsNotFound(friendIds: Vector[User.ID]) extends GetError
      final case class PSQL(cause: PSQLException)                      extends GetError
      final case class Connection(cause: Throwable)                    extends GetError

      sealed trait InternalErrors extends GetError

      object InternalErrors {
        final private[chat] case class UserChatsNotFound(userId: User.ID)
          extends Throwable("Can't find chats by user id")
            with InternalErrors
            with NoStackTrace

        final private[chat] case class UserFriendsNotFound(
            friendIds: Vector[User.ID]
        ) extends Throwable("Can't find user friend by their ids")
            with InternalErrors
            with NoStackTrace
      }

      def fromDatabaseError(error: DatabaseError): GetError =
        error match {
          case DatabaseError.Connection(InternalErrors.UserChatsNotFound(userId)) =>
            GetError.UserChatsNotFound(userId)
          case DatabaseError.Connection(InternalErrors.UserFriendsNotFound(friendIds)) =>
            GetError.UserFriendsNotFound(friendIds)
          case DatabaseError.Connection(th)            => GetError.Connection(th)
          case DatabaseError.Sql(cause: PSQLException) => GetError.PSQL(cause)
          case DatabaseError.Sql(cause)                => GetError.Connection(cause)
        }

      implicit val errorJoiner: SQLErrorJoiner[GetError] = fromDatabaseError(_)
    }
  }

  final case class ChatRow(
      id: Chat.ID,
      initiatorId: User.ID,
      friendId: User.ID,
      createdAt: Instant,
      updatedAt: Instant,
      lastSentMessage: Option[Message.Text],
      lastTimeMessageSent: Option[Instant]
  )
}
