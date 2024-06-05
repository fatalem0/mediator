package mediator.user_chat

import cats.data.NonEmptyList
import derevo.derive
import derevo.tethys.{ tethysReader, tethysWriter }
import mediator.Domain.Offset
import mediator.chat.Domain.Chat
import org.postgresql.util.PSQLException
import sttp.tapir.Schema
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class UserChatsGetError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable
        with NoStackTrace

    object UserChatsGetError extends TofuErrors.Companion[UserChatsGetError] {
      implicit val errorLoggable: Loggable[UserChatsGetError] =
        new SingleValueLoggable[UserChatsGetError] {
          override def logValue(a: UserChatsGetError): LogParamValue =
            LogParamValue(a.code)
        }

      final case object NotFound
        extends UserChatsGetError("USER_CHATS_NOT_FOUND", ErrorLevel.NotFound) {
        val variant: UserChatsGetError = NotFound
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends UserChatsGetError(
          "USER_CHATS_GET_INTERNAL_DATABASE",
          ErrorLevel.Internal
        )

      object InternalDatabase {
        val variant: UserChatsGetError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends UserChatsGetError(
          "USER_CHATS_GET_INTERNAL",
          ErrorLevel.Internal
        )

      object Internal {
        val variant: UserChatsGetError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: NonEmptyList[UserChatsGetError] = NonEmptyList.of(
        NotFound.variant,
        InternalDatabase.variant,
        Internal.variant
      )

      implicit val descriptor: ApiError.Descriptor[UserChatsGetError] =
        new ApiError.Descriptor[UserChatsGetError] {
          override def message(value: UserChatsGetError): ErrorMsg = ErrorMsg {
            value match {
              case Internal(_) | InternalDatabase(_) | NotFound =>
                "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
            }
          }

          override def code(value: UserChatsGetError): ErrorCode   = ErrorCode(value.code)
          override def level(value: UserChatsGetError): ErrorLevel = value.level
        }
    }
  }

  object UserChatsGet {
    @derive(tethysReader, tethysWriter)
    final case class Response(
        prevOffset: Offset,
        chats: Vector[Chat]
    )

    object Response {
      implicit val schema: Schema[Response] =
        Schema.derived[Response].description(
          "Список чатов пользователя"
        )
    }
  }
}
