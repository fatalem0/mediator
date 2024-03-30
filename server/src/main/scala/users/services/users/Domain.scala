package users.services.users

import org.postgresql.util.PSQLException
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import users.Domain.UserEmail
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class UserError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable
        with NoStackTrace

    object UserError extends TofuErrors.Companion[UserError] {
      implicit val errorLoggable: Loggable[UserError] =
        new SingleValueLoggable[UserError] {
          override def logValue(a: UserError): LogParamValue = LogParamValue(
            a.code
          )
        }

      final case class NoUpdate(
          userEmail: UserEmail
      ) extends UserError("USER_NO_UPDATE", ErrorLevel.Business)

      object NoUpdate {
        val variant: UserError = NoUpdate(UserEmail.Example)
      }

      final case class AlreadyExists(
          userEmail: UserEmail
      ) extends UserError("USER_ALREADY_EXISTS", ErrorLevel.AlreadyExists)

      object AlreadyExists {
        val variant: UserError = AlreadyExists(UserEmail.Example)
      }

      final case class NotFound(
          userEmail: UserEmail
      ) extends UserError("USER_NOT_FOUND", ErrorLevel.NotFound)

      object NotFound {
        val variant: UserError = NotFound(UserEmail.Example)
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends UserError("USER_INTERNAL_DATABASE", ErrorLevel.Internal)

      object InternalDatabase {
        val variant: UserError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends UserError("USER_INTERNAL", ErrorLevel.Internal)

      object Internal {
        val variant: UserError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: List[UserError] =
        List(
          NoUpdate.variant,
          AlreadyExists.variant,
          NotFound.variant,
          InternalDatabase.variant,
          Internal.variant
        )

      implicit val descriptor: ApiError.Descriptor[UserError] =
        new ApiError.Descriptor[UserError] {
          override def message(value: UserError): ErrorMsg =
            ErrorMsg {
              value match {
                case NoUpdate(_) =>
                  "Не удалось выполнить запрос"
                case AlreadyExists(_) =>
                  "Пользователь с таким email уже существует"
                case NotFound(_) =>
                  "Пользователя с таким email не существует"
                case Internal(_) | InternalDatabase(_) =>
                  "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
              }
            }

          override def code(value: UserError): ErrorCode = ErrorCode(value.code)
          override def level(value: UserError): ErrorLevel = value.level
        }
    }
  }
}
