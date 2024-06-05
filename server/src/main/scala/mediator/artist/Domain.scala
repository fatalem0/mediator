package mediator.artist

import cats.data.NonEmptyList
import derevo.derive
import derevo.tethys.{ tethysReader, tethysWriter }
import mediator.Domain.Offset
import mediator.db.artist.Domain.ArtistWithGenre
import org.postgresql.util.PSQLException
import sttp.tapir.Schema
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class ArtistsGetError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable
        with NoStackTrace

    object ArtistsGetError extends TofuErrors.Companion[ArtistsGetError] {
      implicit val errorLoggable: Loggable[ArtistsGetError] =
        new SingleValueLoggable[ArtistsGetError] {
          override def logValue(a: ArtistsGetError): LogParamValue =
            LogParamValue(a.code)
        }

      final case object NotFound
        extends ArtistsGetError("ARTISTS_NOT_FOUND", ErrorLevel.NotFound) {
        val variant: ArtistsGetError = NotFound
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends ArtistsGetError(
          "ARTISTS_GET_INTERNAL_DATABASE",
          ErrorLevel.Internal
        )

      object InternalDatabase {
        val variant: ArtistsGetError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends ArtistsGetError("ARTISTS_GET_INTERNAL", ErrorLevel.Internal)

      object Internal {
        val variant: ArtistsGetError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: NonEmptyList[ArtistsGetError] = NonEmptyList.of(
        NotFound.variant,
        InternalDatabase.variant,
        Internal.variant
      )

      implicit val descriptor: ApiError.Descriptor[ArtistsGetError] =
        new ApiError.Descriptor[ArtistsGetError] {
          override def message(value: ArtistsGetError): ErrorMsg = ErrorMsg {
            value match {
              case Internal(_) | InternalDatabase(_) | NotFound =>
                "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
            }
          }

          override def code(value: ArtistsGetError): ErrorCode = ErrorCode(
            value.code
          )

          override def level(value: ArtistsGetError): ErrorLevel = value.level
        }
    }
  }

  object GetArtists {
    @derive(tethysReader, tethysWriter)
    final case class Response(
        prevOffset: Offset,
        artists: Vector[ArtistWithGenre]
    )

    object Response {
      implicit val schema: Schema[Response] =
        Schema.derived[Response].description(
          "Список исполнителей"
        )
    }
  }
}
