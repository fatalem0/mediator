package mediator.services.discogs

import cats.data.NonEmptyList
import tofu.logging.{LogParamValue, Loggable, SingleValueLoggable}
import utils.errors.{ApiError, ErrorLevel}

import scala.util.control.NoStackTrace
import tofu.{Errors => TofuErrors}
import utils.errors.Domain.{ErrorCode, ErrorMsg}

object Domain {
  object Errors {
    sealed abstract class DiscogsTokenError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable
      with NoStackTrace

    object DiscogsTokenError extends TofuErrors.Companion[DiscogsTokenError] {
      implicit val errorLoggable: Loggable[DiscogsTokenError] =
        new SingleValueLoggable[DiscogsTokenError] {
          override def logValue(a: DiscogsTokenError): LogParamValue =
            LogParamValue(a.code)
        }

      final case class Unauthorized(msg: String)
        extends DiscogsTokenError("DISCOGS_AUTH_REQUIRED", ErrorLevel.Forbidden)

      object Unauthorized {
        val variant: DiscogsTokenError = Forbidden("Необходима аутентификация")
      }

      final case class Forbidden(msg: String)
        extends DiscogsTokenError("DISCOGS_FORBIDDEN", ErrorLevel.Forbidden)

      object Forbidden {
        val variant: DiscogsTokenError = Forbidden("Доступ запрещен")
      }

      final case class Internal(cause: Throwable)
        extends DiscogsTokenError("DISCOGS_INTERNAL", ErrorLevel.Internal)

      object Internal {
        val variant: DiscogsTokenError = Internal(new IllegalStateException("Невозможно выполнить запрос"))
      }

      val variants: NonEmptyList[DiscogsTokenError] =
        NonEmptyList.of(
          Unauthorized.variant,
          Forbidden.variant,
          Internal.variant
        )

      implicit val descriptor: ApiError.Descriptor[DiscogsTokenError] =
        new ApiError.Descriptor[DiscogsTokenError] {
          override def message(value: DiscogsTokenError): ErrorMsg =
            ErrorMsg {
              value match {
                case Unauthorized(_) =>
                  "Необходима аутентификация"
                case Forbidden(_) =>
                  "Доступ запрещен"
                case Internal(_) =>
                  "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
              }
            }

          override def code(value: DiscogsTokenError): ErrorCode =
            ErrorCode(value.code)

          override def level(value: DiscogsTokenError): ErrorLevel = value.level
        }
    }
  }

  object Discogs {
    object GetRequestToken {
      final case class Response(
          oauthToken: Token,
          oauthTokenSecret: Token.Secret
      )
    }

    object GetAccessToken {

    }
  }
}
