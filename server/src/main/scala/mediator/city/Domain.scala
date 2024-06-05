package mediator.city

import cats.data.NonEmptyList
import org.postgresql.util.PSQLException
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class CitiesGetError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable
        with NoStackTrace

    object CitiesGetError extends TofuErrors.Companion[CitiesGetError] {
      implicit val errorLoggable: Loggable[CitiesGetError] =
        new SingleValueLoggable[CitiesGetError] {
          override def logValue(a: CitiesGetError): LogParamValue =
            LogParamValue(a.code)
        }

      final case object NotFound extends CitiesGetError(
          "CITIES_GET_NOT_FOUND",
          ErrorLevel.NotFound
        ) {
        val variant: CitiesGetError = NotFound
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends CitiesGetError(
          "CITIES_GET_INTERNAL_DATABASE",
          ErrorLevel.Internal
        )

      object InternalDatabase {
        val variant: CitiesGetError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends CitiesGetError("CITIES_GET_INTERNAL", ErrorLevel.Internal)

      object Internal {
        val variant: CitiesGetError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: NonEmptyList[CitiesGetError] = NonEmptyList.of(
        NotFound.variant,
        InternalDatabase.variant,
        Internal.variant
      )

      implicit val descriptor: ApiError.Descriptor[CitiesGetError] =
        new ApiError.Descriptor[CitiesGetError] {
          override def message(value: CitiesGetError): ErrorMsg = ErrorMsg {
            value match {
              case Internal(_) | InternalDatabase(_) | NotFound =>
                "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
            }
          }

          override def code(value: CitiesGetError): ErrorCode = ErrorCode(
            value.code
          )

          override def level(value: CitiesGetError): ErrorLevel = value.level
        }
    }
  }
}
