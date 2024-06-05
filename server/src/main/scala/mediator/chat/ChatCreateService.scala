package mediator.chat

import cats.effect.Clock
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ Applicative, Monad }
import derevo.derive
import derevo.tagless.applyK
import io.scalaland.chimney.dsl._
import mediator.chat.Domain.Errors.ChatCreateError
import mediator.chat.Domain.{ Chat, ChatCreate }
import mediator.db.chat.check.ChatCheckStorage
import mediator.db.chat.check.Domain.Errors.CheckError
import mediator.db.chat.create.ChatCreateStorage
import mediator.db.chat.create.Domain.Errors.CreateError
import mediator.db.chat.get.Domain.ChatRow
import tofu.generate.GenUUID
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.raise._

@derive(applyK)
trait ChatCreateService[F[_]] {
  def create(req: ChatCreate.Request): F[Either[ChatCreateError, Unit]]
}

object ChatCreateService extends Logging.Companion[ChatCreateService] {
  final private class Impl[F[_]: Monad: Clock: GenUUID: ChatCreateError.Errors](
      chatCreateStorage: ChatCreateStorage[F],
      chatCheckStorage: ChatCheckStorage[F]
  ) extends ChatCreateService[F] {
    override def create(req: ChatCreate.Request): F[Either[ChatCreateError, Unit]] =
      (for {
        _       <- checkIfExists(req)
        chatRow <- createChatRow(req)

        _ <-
          chatCreateStorage
            .create(chatRow)
            .leftMapIn[ChatCreateError] {
              case CreateError.NoUpdate =>
                ChatCreateError.NoUpdate(req.initiatorId, req.friendId)
              case CreateError.AlreadyExists(_) =>
                ChatCreateError.AlreadyExists(req.initiatorId, req.friendId)
              case CreateError.PSQL(cause) =>
                ChatCreateError.InternalDatabase(cause)
              case CreateError.Connection(cause) =>
                ChatCreateError.Internal(cause)
            }
            .reRaise
      } yield ()).attempt[ChatCreateError]

    private def checkIfExists(req: ChatCreate.Request): F[Unit] = {
      chatCheckStorage
        .checkIfExists(req)
        .catchAll[Boolean] {
          case CheckError.PSQL(cause) =>
            ChatCreateError.InternalDatabase(cause).raise[F, Boolean]
          case CheckError.Connection(cause) =>
            ChatCreateError.Internal(cause).raise[F, Boolean]
        }
        .ifM(
          ifTrue = ChatCreateError.AlreadyExists(
            req.initiatorId,
            req.friendId
          ).raise[F, Unit],
          ifFalse = Applicative[F].unit
        )
    }

    private def createChatRow(req: ChatCreate.Request): F[ChatRow] =
      for {
        generatedId <- Chat.ID.create[F]
        now         <- Clock[F].realTimeInstant
      } yield req
        .into[ChatRow]
        .withFieldConst(_.id, generatedId)
        .withFieldConst(_.createdAt, now)
        .withFieldConst(_.updatedAt, now)
        .withFieldConst(_.lastSentMessage, None)
        .withFieldConst(_.lastTimeMessageSent, None)
        .transform
  }

  def make[F[_]: Monad: Clock: GenUUID: ChatCreateError.Errors](
      chatCreateStorage: ChatCreateStorage[F],
      chatCheckStorage: ChatCheckStorage[F]
  ): ChatCreateService[F] = new Impl[F](chatCreateStorage, chatCheckStorage)
}
