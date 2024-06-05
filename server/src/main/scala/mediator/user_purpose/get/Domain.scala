package mediator.user_purpose.get

import cats.data.NonEmptyList
import org.postgresql.util.PSQLException
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class UserPurposesGetError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable
        with NoStackTrace

    object UserPurposesGetError
      extends TofuErrors.Companion[UserPurposesGetError] {
      implicit val errorLoggable: Loggable[UserPurposesGetError] =
        new SingleValueLoggable[UserPurposesGetError] {
          override def logValue(a: UserPurposesGetError): LogParamValue =
            LogParamValue(a.code)
        }

      final case object NotFound extends UserPurposesGetError(
          "USER_PURPOSES_GET_NOT_FOUND",
          ErrorLevel.NotFound
        ) {
        val variant: UserPurposesGetError = NotFound
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends UserPurposesGetError(
          "USER_PURPOSES_GET_INTERNAL_DATABASE",
          ErrorLevel.Internal
        )

      object InternalDatabase {
        val variant: UserPurposesGetError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends UserPurposesGetError(
          "USER_PURPOSES_GET_INTERNAL",
          ErrorLevel.Internal
        )

      object Internal {
        val variant: UserPurposesGetError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: NonEmptyList[UserPurposesGetError] = NonEmptyList.of(
        NotFound.variant,
        InternalDatabase.variant,
        Internal.variant
      )

      implicit val descriptor: ApiError.Descriptor[UserPurposesGetError] =
        new ApiError.Descriptor[UserPurposesGetError] {
          override def message(value: UserPurposesGetError): ErrorMsg =
            ErrorMsg {
              value match {
                case Internal(_) | InternalDatabase(_) | NotFound =>
                  "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
              }
            }

          override def code(value: UserPurposesGetError): ErrorCode   = ErrorCode(value.code)
          override def level(value: UserPurposesGetError): ErrorLevel = value.level
        }
    }
  }
}
