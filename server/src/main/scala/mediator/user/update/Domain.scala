package mediator.user.update

import cats.data.NonEmptyList
import derevo.derive
import derevo.tethys.{ tethysReader, tethysWriter }
import mediator.Domain.{ City, User }
import org.postgresql.util.PSQLException
import sttp.tapir.Schema
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class UpdateUserError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable with NoStackTrace

    object UpdateUserError extends TofuErrors.Companion[UpdateUserError] {
      implicit val errorLoggable: Loggable[UpdateUserError] =
        new SingleValueLoggable[UpdateUserError] {
          override def logValue(a: UpdateUserError): LogParamValue =
            LogParamValue(a.code)
        }

      final case object NoDataForUpdate
        extends UpdateUserError("NO_DATA_FOR_UPDATE", ErrorLevel.BadRequest) {
        val variant: UpdateUserError = NoDataForUpdate
      }

      final case object UserNotUpdated
        extends UpdateUserError("USER_NOT_UPDATED", ErrorLevel.Business) {
        val variant: UpdateUserError = UserNotUpdated
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends UpdateUserError(
          "USER_UPDATE_INTERNAL_DATABASE",
          ErrorLevel.Internal
        )

      object InternalDatabase {
        val variant: UpdateUserError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends UpdateUserError("USER_UPDATE_INTERNAL", ErrorLevel.Internal)

      object Internal {
        val variant: UpdateUserError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: NonEmptyList[UpdateUserError] = NonEmptyList.of(
        NoDataForUpdate.variant,
        UserNotUpdated.variant,
        InternalDatabase.variant,
        Internal.variant
      )

      implicit val descriptor: ApiError.Descriptor[UpdateUserError] =
        new ApiError.Descriptor[UpdateUserError] {
          override def message(value: UpdateUserError): ErrorMsg = ErrorMsg {
            value match {
              case NoDataForUpdate => "Пожалуйста, напишите новые данные"
              case UserNotUpdated =>
                "Не удалось обновить информацию о пользователе. Попробуйте еще раз"
              case Internal(_) | InternalDatabase(_) =>
                "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
            }
          }

          override def code(value: UpdateUserError): ErrorCode   = ErrorCode(value.code)
          override def level(value: UpdateUserError): ErrorLevel = value.level
        }
    }
  }

  object UpdateUser {
    @derive(tethysReader, tethysWriter)
    final case class Request(
        email: Option[User.Email],
        password: Option[User.Password],
        accountName: Option[User.AccountName],
        imageURL: Option[User.ImageURL],
        about: Option[User.About],
        city: Option[City.ID]
    )

    object Request {
      implicit val schema: Schema[Request] = Schema
        .derived[Request]
        .description("Запрос на обновление информации о пользователе")
    }
  }
}
