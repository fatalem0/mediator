package mediator.registration

import cats.data.NonEmptyList
import derevo.derive
import derevo.tethys.{ tethysReader, tethysWriter }
import mediator.Domain.User
import mediator.Domain.User.AccessToken
import mediator.user.create.Domain.Errors.UserCreateError
import sttp.tapir.Schema
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class RegistrationError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable
        with NoStackTrace

    object RegistrationError extends TofuErrors.Companion[RegistrationError] {
      implicit val errorLoggable: Loggable[RegistrationError] =
        new SingleValueLoggable[RegistrationError] {
          override def logValue(a: RegistrationError): LogParamValue =
            LogParamValue(a.code)
        }

      final case class FromUserError(
          error: UserCreateError
      ) extends RegistrationError(error.code, error.level)

      object FromUserError {
        val variant: RegistrationError = FromUserError(
          UserCreateError.NoUpdate.variant
        )
      }

      def fromUserError(
          error: UserCreateError
      ): FromUserError = FromUserError(error)

      val variants: NonEmptyList[RegistrationError] = NonEmptyList.one(
        FromUserError.variant
      )

      implicit val descriptor: ApiError.Descriptor[RegistrationError] =
        new ApiError.Descriptor[RegistrationError] {
          override def message(value: RegistrationError): ErrorMsg = ErrorMsg {
            value match {
              case FromUserError(error) =>
                ApiError.Descriptor[UserCreateError].message(error).value
            }
          }

          override def code(value: RegistrationError): ErrorCode   = ErrorCode(value.code)
          override def level(value: RegistrationError): ErrorLevel = value.level
        }
    }
  }

  object Registration {
    @derive(tethysReader, tethysWriter)
    final case class Request(
        email: User.Email,
        password: User.Password
    )

    object Request {
      implicit val schema: Schema[Request] = Schema
        .derived[Request]
        .description("Данные для регистрации пользователя")
    }

    @derive(tethysReader, tethysWriter)
    final case class Response(
        userId: User.ID,
        accessToken: AccessToken
    )

    object Response {
      implicit val schema: Schema[Response] = Schema.derived[Response]
    }
  }
}
