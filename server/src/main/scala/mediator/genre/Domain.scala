package mediator.genre

import cats.data.NonEmptyList
import org.postgresql.util.PSQLException
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class GenresGetError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable
        with NoStackTrace

    object GenresGetError extends TofuErrors.Companion[GenresGetError] {
      implicit val errorLoggable: Loggable[GenresGetError] =
        new SingleValueLoggable[GenresGetError] {
          override def logValue(a: GenresGetError): LogParamValue =
            LogParamValue(a.code)
        }

      final case object NotFound extends GenresGetError(
          "GENRES_GET_NOT_FOUND",
          ErrorLevel.NotFound
        ) {
        val variant: GenresGetError = NotFound
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends GenresGetError(
          "GENRES_GET_INTERNAL_DATABASE",
          ErrorLevel.Internal
        )

      object InternalDatabase {
        val variant: GenresGetError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends GenresGetError("GENRES_GET_INTERNAL", ErrorLevel.Internal)

      object Internal {
        val variant: GenresGetError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: NonEmptyList[GenresGetError] = NonEmptyList.of(
        NotFound.variant,
        InternalDatabase.variant,
        Internal.variant
      )

      implicit val descriptor: ApiError.Descriptor[GenresGetError] =
        new ApiError.Descriptor[GenresGetError] {
          override def message(value: GenresGetError): ErrorMsg = ErrorMsg {
            value match {
              case Internal(_) | InternalDatabase(_) | NotFound =>
                "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
            }
          }

          override def code(value: GenresGetError): ErrorCode   = ErrorCode(value.code)
          override def level(value: GenresGetError): ErrorLevel = value.level
        }
    }
  }
}
