package users.services.login

import cats.data.NonEmptyList
import derevo.derive
import derevo.tethys.{ tethysReader, tethysWriter }
import org.postgresql.util.PSQLException
import sttp.tapir.Schema
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import users.Domain.{ AccessToken, Password, UserEmail }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class LoginError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable
        with NoStackTrace

    object LoginError extends TofuErrors.Companion[LoginError] {
      implicit val errorLoggable: Loggable[LoginError] =
        new SingleValueLoggable[LoginError] {
          override def logValue(a: LoginError): LogParamValue =
            LogParamValue(a.code)
        }

      final case object WrongPassword
        extends LoginError("LOGIN_WRONG_PASSWORD", ErrorLevel.Forbidden) {
        val variant: LoginError = WrongPassword
      }

      final case object EmailNotExist
        extends LoginError("LOGIN_NOT_FOUND", ErrorLevel.NotFound) {
        val variant: LoginError = EmailNotExist
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends LoginError("LOGIN_INTERNAL_DATABASE", ErrorLevel.Internal)

      object InternalDatabase {
        val variant: LoginError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends LoginError("LOGIN_INTERNAL", ErrorLevel.Internal)

      object Internal {
        val variant: LoginError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: NonEmptyList[LoginError] =
        NonEmptyList.of(
          WrongPassword.variant,
          EmailNotExist.variant,
          InternalDatabase.variant,
          Internal.variant
        )

      implicit val descriptor: ApiError.Descriptor[LoginError] =
        new ApiError.Descriptor[LoginError] {
          override def message(value: LoginError): ErrorMsg =
            ErrorMsg {
              value match {
                case WrongPassword =>
                  "Неправильный пароль, попробуйте еще раз"
                case EmailNotExist =>
                  "Пользователя с таким email не существует"
                case Internal(_) | InternalDatabase(_) =>
                  "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
              }
            }

          override def code(value: LoginError): ErrorCode =
            ErrorCode(value.code)
          override def level(value: LoginError): ErrorLevel = value.level
        }
    }
  }

  object Login {
    @derive(tethysReader, tethysWriter)
    final case class Request(
        email: UserEmail,
        password: Password
    )

    object Request {
      implicit val schema: Schema[Request] =
        Schema
          .derived[Request]
          .description("Данные для входа пользователя")
    }

    @derive(tethysReader, tethysWriter)
    final case class Response(accessToken: AccessToken)

    object Response {
      implicit val schema: Schema[Response] = Schema.derived[Response]
    }
  }
}
