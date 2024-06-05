package mediator.user.create

import mediator.Domain.User
import org.postgresql.util.PSQLException
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class UserCreateError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable
        with NoStackTrace

    object UserCreateError extends TofuErrors.Companion[UserCreateError] {
      implicit val errorLoggable: Loggable[UserCreateError] =
        new SingleValueLoggable[UserCreateError] {
          override def logValue(a: UserCreateError): LogParamValue =
            LogParamValue(
              a.code
            )
        }

      final case class NoUpdate(
          userEmail: User.Email
      ) extends UserCreateError("USER_CREATE_NO_UPDATE", ErrorLevel.Business)

      object NoUpdate {
        val variant: UserCreateError = NoUpdate(User.Email.Example)
      }

      final case class AlreadyExists(
          userEmail: User.Email
      ) extends UserCreateError(
          "USER_CREATE_ALREADY_EXISTS",
          ErrorLevel.AlreadyExists
        )

      object AlreadyExists {
        val variant: UserCreateError = AlreadyExists(User.Email.Example)
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends UserCreateError(
          "USER_CREATE_INTERNAL_DATABASE",
          ErrorLevel.Internal
        )

      object InternalDatabase {
        val variant: UserCreateError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends UserCreateError("USER_CREATE_INTERNAL", ErrorLevel.Internal)

      object Internal {
        val variant: UserCreateError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: List[UserCreateError] = List(
        NoUpdate.variant,
        AlreadyExists.variant,
        InternalDatabase.variant,
        Internal.variant
      )

      implicit val descriptor: ApiError.Descriptor[UserCreateError] =
        new ApiError.Descriptor[UserCreateError] {
          override def message(value: UserCreateError): ErrorMsg = ErrorMsg {
            value match {
              case NoUpdate(_) =>
                "Не удалось выполнить запрос"
              case AlreadyExists(_) =>
                "Пользователь с таким email уже существует"
              case Internal(_) | InternalDatabase(_) =>
                "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
            }
          }

          override def code(value: UserCreateError): ErrorCode   = ErrorCode(value.code)
          override def level(value: UserCreateError): ErrorLevel = value.level
        }
    }
  }
}
