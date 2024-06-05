package mediator.db.chat.get

import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import cats.{ FlatMap, MonadThrow }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import io.scalaland.chimney.dsl._
import mediator.Domain.{ Limit, Offset, User }
import mediator.chat.Domain.Chat
import mediator.chat.Domain.Chat.LastTimeMessageSent
import mediator.db.chat.ChatStorage
import mediator.db.chat.get.Domain.ChatRow
import mediator.db.chat.get.Domain.Errors.GetError
import mediator.db.user.UserStorage
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.foption._
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

import java.time.{ LocalDateTime, ZoneId }

@derive(applyK)
trait ChatGetStorage[F[_]] {
  def getByUserId(
      limit: Limit,
      offset: Offset,
      userId: User.ID
  ): F[Either[GetError, Vector[Chat]]]
}

object ChatGetStorage extends Logging.Companion[ChatGetStorage] {
  final private class Impl[F[_]: MonadThrow](
      chatStorage: ChatStorage[F],
      userStorage: UserStorage[F]
  ) extends ChatGetStorage[F] {
    override def getByUserId(
        limit: Limit,
        offset: Offset,
        userId: User.ID
    ): F[Either[GetError, Vector[Chat]]] =
      for {
        chatRows <-
          chatStorage.getByUserId(limit, offset, userId)
            .map(vector =>
              Either.cond(
                vector.nonEmpty,
                vector,
                GetError.InternalErrors.UserChatsNotFound(userId)
              )
            )
            .reRaise

        userFriendIds = chatRows.map(_.friendId)

        userFriends <-
          userFriendIds
            .traverse(userStorage.getByID)
            .map(_.sequence)
            .toRightIn(
              GetError.InternalErrors.UserFriendsNotFound(userFriendIds)
            )
            .reRaise

        res = chatRows
          .zip(userFriends.map(user => (user.accountName, user.imageURL)))
          .map { case (chatRow, userNameAndImage) =>
            transformIntoChat(chatRow, userNameAndImage)
          }
      } yield Right(res)

    private def transformIntoChat(
        chatRow: ChatRow,
        userNameAndImage: (Option[User.AccountName], Option[User.ImageURL])
    ): Chat =
      chatRow
        .into[Chat]
        .withFieldConst(_.friendAccountName, userNameAndImage._1)
        .withFieldConst(_.friendImageUrl, userNameAndImage._2)
        .withFieldConst(
          _.lastTimeMessageSent,
          chatRow.lastTimeMessageSent.map(instant =>
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
          )
            .map(ldt => LastTimeMessageSent(s"${ldt.getHour}:${ldt.getMinute}"))
        )
        .transform
  }

  final private class LogMid[F[_]: FlatMap: ChatGetStorage.Log]
    extends ChatGetStorage[Mid[F, *]] {
    override def getByUserId(
        limit: Limit,
        offset: Offset,
        userId: User.ID
    ): Mid[F, Either[GetError, Vector[Chat]]] =
      debug"Trying to get chats for user = $userId with limit = $limit and offset = $offset" *>
        _.flatTap {
          case Left(GetError.UserChatsNotFound(userId)) =>
            warn"Can't find chats for user = $userId with limit = $limit and offset = $offset"
          case Left(GetError.UserFriendsNotFound(friendIds)) =>
            warn"Can't find friend with ids = $friendIds for user = $userId with limit = $limit and offset = $offset"
          case Left(GetError.PSQL(cause)) =>
            errorCause"Failed to get chats for user = $userId with limit = $limit and offset = $offset" (
              cause
            )
          case Left(GetError.Connection(cause)) =>
            errorCause"Failed to get chats for user = $userId with limit = $limit and offset = $offset due to connection error" (
              cause
            )
          case Right(_) =>
            debug"Successfully got chats for user = $userId with limit = $limit and offset = $offset"
        }
  }

  private object Errors extends ChatGetStorage[SQLErrorJoiner] {
    override def getByUserId(
        limit: Limit,
        offset: Offset,
        userId: User.ID
    ): SQLErrorJoiner[Either[GetError, Vector[Chat]]] =
      SQLErrorJoiner[Either[GetError, Vector[Chat]]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: ChatGetStorage[F] =
    DatabaseRunner[ChatGetStorage, F].wire(
      new Impl[ConnectionIO](ChatStorage.db, UserStorage.db),
      Errors
    )

  def makeObservable[
      F[_]: MonadCancelThrow: SafeTransactor: Logging.Make
  ]: ChatGetStorage[F] = {
    val logMid = new LogMid[F]: ChatGetStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
