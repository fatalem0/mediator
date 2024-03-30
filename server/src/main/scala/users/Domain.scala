package users

import cats.Functor
import cats.syntax.functor._
import com.outr.scalapass.Argon2PasswordFactory
import derevo.derive
import derevo.tethys.{ tethysReader, tethysWriter }
import doobie.Meta
import doobie.postgres.implicits._
import io.estatico.newtype.macros.newtype
import sttp.tapir.Schema
import tofu.generate.GenUUID
import tofu.logging.derivation.loggable
import users.Domain.Password.HashedUserPassword
import users.Domain.UserData.UserId
import utils.tethys._

import java.time.Instant
import java.util.UUID

object Domain {
  @derive(tethysReader)
  final case class UserData(
      id: UserId,
      email: UserEmail,
      hashedPassword: HashedUserPassword,
      createdAt: Instant,
      updatedAt: Instant
  )

  object UserData {
    @derive(tethysReader, tethysWriter, loggable)
    @newtype final case class UserId(value: UUID)

    object UserId {
      def create[F[_]: Functor: GenUUID]: F[UserData.UserId] =
        GenUUID.random[F].map(apply)

      implicit val meta: Meta[UserId] = Meta[UUID].imap(apply)(_.value)
    }
  }

  @derive(tethysReader, tethysWriter, loggable)
  @newtype final case class UserEmail(value: String)

  object UserEmail {
    implicit val meta: Meta[UserEmail] = Meta[String].imap(apply)(_.value)

    implicit val schema: Schema[UserEmail] =
      Schema.string[UserEmail].description("Email пользователя")

    final val Example = UserEmail("some-email")
  }

  sealed abstract class Password(value: String)

  object Password {
    @derive(tethysReader, tethysWriter)
    final case class UnhashedUserPassword(value: String)
      extends Password(value) {
      def hashPassword: HashedUserPassword =
        HashedUserPassword(factory.hash(this.value))
    }

    object UnhashedUserPassword {
      implicit val schema: Schema[UnhashedUserPassword] =
        Schema
          .string[UnhashedUserPassword]
          .description("Нехешированный пароль пользователя")
    }

    @derive(tethysReader, tethysWriter)
    final case class HashedUserPassword(value: String) extends Password(value) {
      def verifyHashedPassword(
          attemptedPassword: UnhashedUserPassword
      ): Boolean =
        factory.verify(attemptedPassword.value, this.value)
    }

    object HashedUserPassword {
      implicit val schema: Schema[HashedUserPassword] =
        Schema
          .string[HashedUserPassword]
          .description("Хешированный пароль пользователя")
    }

    private val factory = Argon2PasswordFactory()
  }

  @derive(tethysReader, tethysWriter)
  @newtype final case class AccessToken(value: String)

  object AccessToken {
    implicit val schema: Schema[AccessToken] =
      Schema.string[AccessToken].description("Access token пользователя")
  }
}
