package mediator.favorite_artist

import cats.data.NonEmptyList
import derevo.derive
import derevo.tethys.{ tethysReader, tethysWriter }
import mediator.Domain.Artist
import org.postgresql.util.PSQLException
import sttp.tapir.Schema
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class UpdateUserFavoriteArtistsError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable with NoStackTrace

    object UpdateUserFavoriteArtistsError
      extends TofuErrors.Companion[UpdateUserFavoriteArtistsError] {
      implicit val errorLoggable: Loggable[UpdateUserFavoriteArtistsError] =
        new SingleValueLoggable[UpdateUserFavoriteArtistsError] {
          override def logValue(
              a: UpdateUserFavoriteArtistsError
          ): LogParamValue = LogParamValue(a.code)
        }

      final case object SmallNumberOfFavoriteArtists
        extends UpdateUserFavoriteArtistsError(
          "SMALL_NUMBER_OF_FAVORITE_ARTISTS",
          ErrorLevel.BadRequest
        ) {
        val variant: UpdateUserFavoriteArtistsError =
          SmallNumberOfFavoriteArtists
      }

      final case object UserFavoriteArtistsNotUpdated
        extends UpdateUserFavoriteArtistsError(
          "USER_FAVORITE_ARTISTS_NOT_UPDATED",
          ErrorLevel.Business
        ) {
        val variant: UpdateUserFavoriteArtistsError =
          UserFavoriteArtistsNotUpdated
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends UpdateUserFavoriteArtistsError(
          "USER_FAVORITE_ARTISTS_UPDATE_INTERNAL_DATABASE",
          ErrorLevel.Internal
        )

      object InternalDatabase {
        val variant: UpdateUserFavoriteArtistsError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends UpdateUserFavoriteArtistsError(
          "USER_FAVORITE_ARTISTS_UPDATE_INTERNAL",
          ErrorLevel.Internal
        )

      object Internal {
        val variant: UpdateUserFavoriteArtistsError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: NonEmptyList[UpdateUserFavoriteArtistsError] =
        NonEmptyList.of(
          SmallNumberOfFavoriteArtists.variant,
          UserFavoriteArtistsNotUpdated.variant,
          InternalDatabase.variant,
          Internal.variant
        )

      implicit val descriptor: ApiError.Descriptor[
        UpdateUserFavoriteArtistsError
      ] =
        new ApiError.Descriptor[UpdateUserFavoriteArtistsError] {
          override def message(
              value: UpdateUserFavoriteArtistsError
          ): ErrorMsg = ErrorMsg {
            value match {
              case SmallNumberOfFavoriteArtists =>
                "Нужно выбрать минимум 3 любимых исполнителя"
              case UserFavoriteArtistsNotUpdated =>
                "Не удалось обновить список любимых исполнителей. Попробуйте еще раз"
              case Internal(_) | InternalDatabase(_) =>
                "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
            }
          }

          override def code(value: UpdateUserFavoriteArtistsError): ErrorCode =
            ErrorCode(value.code)

          override def level(
              value: UpdateUserFavoriteArtistsError
          ): ErrorLevel = value.level
        }
    }
  }

  object UpdateUserFavoriteArtists {
    @derive(tethysReader, tethysWriter)
    final case class Request(favoriteArtistIds: Vector[Artist.ID])

    object Request {
      implicit val schema: Schema[Request] = Schema
        .derived[Request]
        .description(
          "Запрос на обновление списка любимых исполнителей пользователя"
        )
    }
  }
}
