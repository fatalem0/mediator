package mediator.user_purpose.update

import cats.data.NonEmptyList
import derevo.derive
import derevo.tethys.{ tethysReader, tethysWriter }
import mediator.Domain.UserPurpose
import org.postgresql.util.PSQLException
import sttp.tapir.Schema
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class UpdateUserPurposesError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable with NoStackTrace

    object UpdateUserPurposesError
      extends TofuErrors.Companion[UpdateUserPurposesError] {
      implicit val errorLoggable: Loggable[UpdateUserPurposesError] =
        new SingleValueLoggable[UpdateUserPurposesError] {
          override def logValue(a: UpdateUserPurposesError): LogParamValue =
            LogParamValue(a.code)
        }

      final case object NoUserPurposes
        extends UpdateUserPurposesError(
          "NO_USER_PURPOSES",
          ErrorLevel.BadRequest
        ) {
        val variant: UpdateUserPurposesError = NoUserPurposes
      }

      final case object UserPurposesNotUpdated
        extends UpdateUserPurposesError(
          "USER_PURPOSES_NOT_UPDATED",
          ErrorLevel.Business
        ) {
        val variant: UpdateUserPurposesError = UserPurposesNotUpdated
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends UpdateUserPurposesError(
          "USER_PURPOSES_UPDATE_INTERNAL_DATABASE",
          ErrorLevel.Internal
        )

      object InternalDatabase {
        val variant: UpdateUserPurposesError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends UpdateUserPurposesError(
          "USER_UPDATE_INTERNAL",
          ErrorLevel.Internal
        )

      object Internal {
        val variant: UpdateUserPurposesError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: NonEmptyList[UpdateUserPurposesError] = NonEmptyList.of(
        NoUserPurposes.variant,
        UserPurposesNotUpdated.variant,
        InternalDatabase.variant,
        Internal.variant
      )

      implicit val descriptor: ApiError.Descriptor[UpdateUserPurposesError] =
        new ApiError.Descriptor[UpdateUserPurposesError] {
          override def message(value: UpdateUserPurposesError): ErrorMsg =
            ErrorMsg {
              value match {
                case NoUserPurposes =>
                  "Нужно выбрать хотя бы одну цель использования приложения"
                case UserPurposesNotUpdated =>
                  "Не удалось обновить список целей использования приложения. Попробуйте еще раз"
                case Internal(_) | InternalDatabase(_) =>
                  "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
              }
            }

          override def code(value: UpdateUserPurposesError): ErrorCode =
            ErrorCode(value.code)

          override def level(value: UpdateUserPurposesError): ErrorLevel =
            value.level
        }
    }
  }

  object UpdateUserPurposes {
    @derive(tethysReader, tethysWriter)
    final case class Request(userPurposeIds: Vector[UserPurpose.ID])

    object Request {
      implicit val schema: Schema[Request] = Schema
        .derived[Request]
        .description(
          "Запрос на обновление списка целей использования приложения"
        )
    }
  }
}
