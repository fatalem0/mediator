package mediator.potential_friend

import cats.data.NonEmptyList
import derevo.derive
import derevo.tethys.{ tethysReader, tethysWriter }
import doobie.Meta
import io.estatico.newtype.macros.newtype
import mediator.Domain._
import org.postgresql.util.PSQLException
import sttp.tapir.Schema
import tofu.logging.{ Loggable, LogParamValue, SingleValueLoggable }
import tofu.{ Errors => TofuErrors }
import utils.errors.Domain.{ ErrorCode, ErrorMsg }
import utils.errors.{ ApiError, ErrorLevel }

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed abstract class UserPotentialFriendsGetError(
        val code: String,
        val level: ErrorLevel
    ) extends Throwable
        with NoStackTrace

    object UserPotentialFriendsGetError
      extends TofuErrors.Companion[UserPotentialFriendsGetError] {
      implicit val errorLoggable: Loggable[UserPotentialFriendsGetError] =
        new SingleValueLoggable[UserPotentialFriendsGetError] {
          override def logValue(
              a: UserPotentialFriendsGetError
          ): LogParamValue = LogParamValue(a.code)
        }

      final case object NotFound extends UserPotentialFriendsGetError(
          "USER_POTENTIAL_FRIENDS_GET_NOT_FOUND",
          ErrorLevel.NotFound
        ) {
        val variant: UserPotentialFriendsGetError = NotFound
      }

      final case class InternalDatabase(
          cause: PSQLException
      ) extends UserPotentialFriendsGetError(
          "USER_POTENTIAL_FRIENDS_GET_INTERNAL_DATABASE",
          ErrorLevel.Internal
        )

      object InternalDatabase {
        val variant: UserPotentialFriendsGetError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      final case class Internal(
          cause: Throwable
      ) extends UserPotentialFriendsGetError(
          "USER_POTENTIAL_FRIENDS_GET_INTERNAL",
          ErrorLevel.Internal
        )

      object Internal {
        val variant: UserPotentialFriendsGetError = Internal(
          new IllegalStateException(
            "Невозможно обновить запись, она уже существует"
          )
        )
      }

      val variants: NonEmptyList[UserPotentialFriendsGetError] =
        NonEmptyList.of(
          NotFound.variant,
          InternalDatabase.variant,
          Internal.variant
        )

      implicit val descriptor: ApiError.Descriptor[
        UserPotentialFriendsGetError
      ] =
        new ApiError.Descriptor[UserPotentialFriendsGetError] {
          override def message(value: UserPotentialFriendsGetError): ErrorMsg =
            ErrorMsg {
              value match {
                case Internal(_) | InternalDatabase(_) | NotFound =>
                  "Непредвиденная ошибка. Пожалуйста, попробуйте позже"
              }
            }

          override def code(value: UserPotentialFriendsGetError): ErrorCode =
            ErrorCode(value.code)

          override def level(value: UserPotentialFriendsGetError): ErrorLevel =
            value.level
        }
    }
  }

  object GetPotentialFriends {
    @derive(tethysReader, tethysWriter)
    final case class Response(
        prevOffset: Offset,
        potentialFriends: Vector[PotentialFriend]
    )

    object Response {
      implicit val schema: Schema[Response] =
        Schema.derived[Response].description(
          "Список потенциальных друзей"
        )
    }
  }

  @derive(tethysReader, tethysWriter)
  final case class PotentialFriend(
      id: User.ID,
      accountName: User.AccountName,
      imageURL: Option[User.ImageURL],
      about: User.About,
      city: City.Name,
      userPurposes: Vector[UserPurpose.Name],
      favoriteGenres: Vector[Genre.Name],
      favoriteArtists: Vector[Artist.Name],
      matchingPercent: MatchingPercent
  )

  object PotentialFriend {
    implicit val schema: Schema[PotentialFriend] =
      Schema.derived[PotentialFriend].description(
        "Список потенциальных друзей пользователя"
      )
  }

  @derive(tethysReader, tethysWriter)
  @newtype final case class MatchingPercent(value: Double)

  object MatchingPercent {
    implicit val meta: Meta[MatchingPercent] = Meta[Double].imap(apply)(_.value)

    implicit val schema: Schema[MatchingPercent] = Schema
      .schemaForDouble
      .map(matchingPercent => Some(apply(matchingPercent)))(_.value)
      .description(
        "Процент совпадения интересов пользователя с другим пользователем"
      )
  }
}
