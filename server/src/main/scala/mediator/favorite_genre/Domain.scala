package mediator.favorite_genre

import cats.data.NonEmptyList
import derevo.derive
import derevo.tethys.{ tethysReader, tethysWriter }
import mediator.Domain.Genre
import org.postgresql.util.PSQLException
import sttp.tapir.Schema
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class UpdateUserFavoriteGenresError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable with NoStackTrace

    object UpdateUserFavoriteGenresError
      extends TofuErrors.Companion[UpdateUserFavoriteGenresError] {
      implicit val errorLoggable: Loggable[UpdateUserFavoriteGenresError] =
        new SingleValueLoggable[UpdateUserFavoriteGenresError] {
          override def logValue(
              a: UpdateUserFavoriteGenresError
          ): LogParamValue = LogParamValue(a.code)
        }

      final case object NoFavoriteGenres
        extends UpdateUserFavoriteGenresError(
          "NO_FAVORITE_GENRES",
          ErrorLevel.BadRequest
        ) {
        val variant: UpdateUserFavoriteGenresError = NoFavoriteGenres
      }

      final case object UserFavoriteGenresNotUpdated
        extends UpdateUserFavoriteGenresError(
          "USER_FAVORITE_GENRES_NOT_UPDATED",
          ErrorLevel.Business
        ) {
        val variant: UpdateUserFavoriteGenresError =
          UserFavoriteGenresNotUpdated
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends UpdateUserFavoriteGenresError(
          "USER_FAVORITE_GENRES_UPDATE_INTERNAL_DATABASE",
          ErrorLevel.Internal
        )

      object InternalDatabase {
        val variant: UpdateUserFavoriteGenresError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends UpdateUserFavoriteGenresError(
          "USER_FAVORITE_GENRES_UPDATE_INTERNAL",
          ErrorLevel.Internal
        )

      object Internal {
        val variant: UpdateUserFavoriteGenresError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: NonEmptyList[UpdateUserFavoriteGenresError] =
        NonEmptyList.of(
          NoFavoriteGenres.variant,
          UserFavoriteGenresNotUpdated.variant,
          InternalDatabase.variant,
          Internal.variant
        )

      implicit val descriptor: ApiError.Descriptor[
        UpdateUserFavoriteGenresError
      ] =
        new ApiError.Descriptor[UpdateUserFavoriteGenresError] {
          override def message(value: UpdateUserFavoriteGenresError): ErrorMsg =
            ErrorMsg {
              value match {
                case NoFavoriteGenres =>
                  "Нужно выбрать минимум 1 любимый жанр музыки"
                case UserFavoriteGenresNotUpdated =>
                  "Не удалось обновить список любимых жанров музыки. Попробуйте еще раз"
                case Internal(_) | InternalDatabase(_) =>
                  "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
              }
            }

          override def code(value: UpdateUserFavoriteGenresError): ErrorCode =
            ErrorCode(value.code)

          override def level(value: UpdateUserFavoriteGenresError): ErrorLevel =
            value.level
        }
    }
  }

  object UpdateUserFavoriteGenres {
    @derive(tethysReader, tethysWriter)
    final case class Request(favoriteGenreIds: Vector[Genre.ID])

    object Request {
      implicit val schema: Schema[Request] = Schema
        .derived[Request]
        .description(
          "Запрос на обновление списка любимых жанров музыки пользователя"
        )
    }
  }
}
