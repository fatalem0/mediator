package mediator.chat

import cats.Functor
import cats.data.NonEmptyList
import cats.syntax.functor._
import derevo.derive
import doobie.postgres.implicits._
import derevo.tethys.{ tethysReader, tethysWriter }
import doobie.Meta
import io.estatico.newtype.macros.newtype
import mediator.Domain.User
import org.postgresql.util.PSQLException
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.{ Codec, Schema }
import tofu.generate.GenUUID
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.logging.derivation.loggable
import tofu.{ Errors => TofuErrors }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }
import utils.tethys._

import java.time.Instant
import java.util.UUID
import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class ChatCreateError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable
        with NoStackTrace

    object ChatCreateError extends TofuErrors.Companion[ChatCreateError] {
      implicit val errorLoggable: Loggable[ChatCreateError] =
        new SingleValueLoggable[ChatCreateError] {
          override def logValue(a: ChatCreateError): LogParamValue =
            LogParamValue(
              a.code
            )
        }

      final case class NoUpdate(
          initiatorID: User.ID,
          friendID: User.ID
      ) extends ChatCreateError("CHAT_CREATE_NO_UPDATE", ErrorLevel.Business)

      object NoUpdate {
        val variant: ChatCreateError = NoUpdate(
          User.ID.Example,
          User.ID.Example
        )
      }

      final case class AlreadyExists(
          initiatorID: User.ID,
          friendID: User.ID
      ) extends ChatCreateError(
          "CHAT_CREATE_ALREADY_EXISTS",
          ErrorLevel.AlreadyExists
        )

      object AlreadyExists {
        val variant: ChatCreateError = AlreadyExists(
          User.ID.Example,
          User.ID.Example
        )
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends ChatCreateError(
          "CHAT_CREATE_INTERNAL_DATABASE",
          ErrorLevel.Internal
        )

      object InternalDatabase {
        val variant: ChatCreateError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends ChatCreateError("CHAT_CREATE_INTERNAL", ErrorLevel.Internal)

      object Internal {
        val variant: ChatCreateError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: NonEmptyList[ChatCreateError] = NonEmptyList.of(
        NoUpdate.variant,
        AlreadyExists.variant,
        InternalDatabase.variant,
        Internal.variant
      )

      implicit val descriptor: ApiError.Descriptor[ChatCreateError] =
        new ApiError.Descriptor[ChatCreateError] {
          override def message(value: ChatCreateError): ErrorMsg = ErrorMsg {
            value match {
              case NoUpdate(_, _) => "Не удалось выполнить запрос"
              case AlreadyExists(_, _) =>
                "Чат с данным пользователем уже существует"
              case Internal(_) | InternalDatabase(_) =>
                "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
            }
          }

          override def code(value: ChatCreateError): ErrorCode = ErrorCode(
            value.code
          )

          override def level(value: ChatCreateError): ErrorLevel = value.level
        }
    }
  }

  object ChatCreate {
    @derive(tethysReader, tethysWriter)
    final case class Request(
        initiatorId: User.ID,
        friendId: User.ID
    )

    object Request {
      implicit val schema: Schema[Request] = Schema
        .derived[Request]
        .description("Запрос на создание чата")
    }
  }

  @derive(tethysReader, tethysWriter)
  final case class Chat(
      id: Chat.ID,
      initiatorId: User.ID,
      friendId: User.ID,
      friendAccountName: Option[User.AccountName],
      friendImageUrl: Option[User.ImageURL],
      lastSentMessage: Option[Message.Text],
      lastTimeMessageSent: Option[Chat.LastTimeMessageSent]
  )

  object Chat {
    @derive(tethysReader, tethysWriter, loggable)
    @newtype final case class ID(value: UUID)

    object ID {
      def create[F[_]: Functor: GenUUID]: F[ID] = GenUUID.random[F].map(apply)

      implicit val codec: PlainCodec[ID] = Codec.uuid.map(apply _)(_.value)
      implicit val meta: Meta[ID]        = Meta[UUID].imap(apply)(_.value)
      implicit val schema: Schema[ID]    = Schema.string[ID].description("ID чата")
    }

    @derive(tethysReader, tethysWriter)
    @newtype final case class LastTimeMessageSent(value: String)

    object LastTimeMessageSent {
      implicit val meta: Meta[LastTimeMessageSent] =
        Meta[String].imap(apply)(_.value)

      implicit val schema: Schema[LastTimeMessageSent] = Schema.string[
        LastTimeMessageSent
      ].description(
        "Время отправки последнего сообщения в чате"
      )
    }

    implicit val schema: Schema[Chat] = Schema.derived[Chat].description(
      "Пользовательский чат"
    )
  }

//  @derive(tethysReader, tethysWriter)
  final case class Message(
      id: Message.ID,
      chatID: Chat.ID,
      senderID: User.ID,
      text: Message.Text,
      createdAt: Instant
  )

  object Message {
    @derive(tethysReader, tethysWriter, loggable)
    @newtype final case class ID(value: UUID)

    object ID {
      implicit val codec: PlainCodec[ID] = Codec.uuid.map(apply _)(_.value)
      implicit val meta: Meta[ID]        = Meta[UUID].imap(apply)(_.value)
      implicit val schema: Schema[ID] =
        Schema.string[ID].description("ID сообщения")
    }

    @derive(tethysReader, tethysWriter)
    @newtype final case class Text(value: String)

    object Text {
      implicit val meta: Meta[Text] = Meta[String].imap(apply)(_.value)
      implicit val schema: Schema[Text] =
        Schema.string[Text].description("Текст сообщения")
    }

    implicit val schema: Schema[Message] =
      Schema.derived[Message].description("Сообщение")
  }
}
