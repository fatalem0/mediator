package mediator.user_chat

import cats.Monad
import derevo.derive
import derevo.tagless.applyK
import mediator.Domain.{ Limit, Offset, User }
import mediator.db.chat.get.ChatGetStorage
import mediator.db.chat.get.Domain.Errors.GetError
import mediator.user_chat.Domain.Errors.UserChatsGetError
import mediator.user_chat.Domain.UserChatsGet
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.raise._

@derive(applyK)
trait UserChatsGetService[F[_]] {
  def get(
      limit: Limit,
      offset: Offset,
      userId: User.ID
  ): F[Either[UserChatsGetError, UserChatsGet.Response]]
}

object UserChatsGetService extends Logging.Companion[UserChatsGetService] {
  final private class Impl[F[_]: Monad: UserChatsGetError.Errors](
      storage: ChatGetStorage[F]
  ) extends UserChatsGetService[F] {
    override def get(
        limit: Limit,
        offset: Offset,
        userId: User.ID
    ): F[Either[UserChatsGetError, UserChatsGet.Response]] =
      storage.getByUserId(limit, offset, userId)
        .mapIn(UserChatsGet.Response(offset, _))
        .catchAll[UserChatsGet.Response] {
          case GetError.UserChatsNotFound(_) | GetError.UserFriendsNotFound(_) =>
            UserChatsGetError.NotFound.raise[F, UserChatsGet.Response]
          case GetError.PSQL(cause) =>
            UserChatsGetError.InternalDatabase(cause).raise[F, UserChatsGet.Response]
          case GetError.Connection(cause) =>
            UserChatsGetError.Internal(cause).raise[F, UserChatsGet.Response]
        }
        .attempt[UserChatsGetError]
  }

  def make[F[_]: Monad: UserChatsGetError.Errors](
      storage: ChatGetStorage[F]
  ): UserChatsGetService[F] = new Impl[F](storage)
}
