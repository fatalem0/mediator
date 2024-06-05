package mediator

import cats.Functor
import cats.syntax.functor._
import com.outr.scalapass.Argon2PasswordFactory
import derevo.derive
import derevo.tethys.{ tethysReader, tethysWriter }
import doobie.Meta
import doobie.postgres.implicits._
import io.estatico.newtype.macros.newtype
import mediator.Domain.User.Password.factory
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.{ Codec, Schema }
import tofu.generate.GenUUID
import tofu.logging.derivation.loggable
import utils.tethys._

import java.time.Instant
import java.util.UUID

object Domain {
  final case class User(
      id: User.ID,
      email: User.Email,
      hashedPassword: User.Password,
      createdAt: Instant,
      updatedAt: Instant,
      accountName: Option[User.AccountName],
      imageURL: Option[User.ImageURL],
      about: Option[User.About],
      cityID: Option[City.ID]
  )

  object User {
    @derive(tethysReader, tethysWriter, loggable)
    @newtype final case class ID(value: UUID)

    object ID {
      def create[F[_]: Functor: GenUUID]: F[ID] = GenUUID.random[F].map(apply)

      implicit val codec: PlainCodec[ID] = Codec.uuid.map(apply _)(_.value)
      implicit val meta: Meta[ID]        = Meta[UUID].imap(apply)(_.value)

      implicit val schema: Schema[ID] =
        Schema.string[ID].description("ID пользователя")

      final val Example = ID(
        UUID.fromString("625e06db-6ace-4de1-abf4-1cfb135854ca")
      )
    }

    @derive(tethysReader, tethysWriter, loggable)
    @newtype final case class Email(value: String)

    object Email {
      implicit val meta: Meta[Email] = Meta[String].imap(apply)(_.value)

      implicit val schema: Schema[Email] = Schema.string[Email].description(
        "Email пользователя"
      )

      final val Example = Email("some-email")
    }

    @derive(tethysReader, tethysWriter)
    @newtype final case class Password(value: String) {
      def hashPassword: Password = Password(factory.hash(this.value))

      def verifyHashedPassword(
          attemptedPassword: Password
      ): Boolean = factory.verify(attemptedPassword.value, this.value)
    }

    object Password {
      implicit val meta: Meta[Password] = Meta[String].imap(apply)(_.value)

      implicit val schema: Schema[Password] = Schema
        .string[Password]
        .description("Пароль пользователя")

      private val factory = Argon2PasswordFactory()
    }

    @derive(tethysReader, tethysWriter)
    @newtype final case class AccountName(value: String)

    object AccountName {
      implicit val meta: Meta[AccountName] = Meta[String].imap(apply)(_.value)

      implicit val schema: Schema[AccountName] = Schema
        .string[AccountName]
        .description("Имя аккаунта пользователя")
    }

    @derive(tethysReader, tethysWriter)
    @newtype final case class ImageURL(value: String)

    object ImageURL {
      implicit val meta: Meta[ImageURL] = Meta[String].imap(apply)(_.value)

      implicit val schema: Schema[ImageURL] = Schema
        .string[ImageURL]
        .description("URL на фото пользоватея")
    }

    @derive(tethysReader, tethysWriter)
    @newtype final case class About(value: String)

    object About {
      implicit val meta: Meta[About] = Meta[String].imap(apply)(_.value)

      implicit val schema: Schema[About] = Schema
        .string[About]
        .description("Информация о пользователе пользователя")
    }

    @derive(tethysReader, tethysWriter)
    @newtype final case class AccessToken(value: String)

    object AccessToken {
      implicit val schema: Schema[AccessToken] =
        Schema.string[AccessToken].description(
          "Access token пользователя"
        )
    }

    object Genre {
      @derive(tethysReader, tethysWriter, loggable)
      @newtype final case class ID(value: UUID)

      object ID {
        def create[F[_]: Functor: GenUUID]: F[ID] = GenUUID.random[F].map(apply)

        implicit val codec: PlainCodec[ID] = Codec.uuid.map(apply _)(_.value)
        implicit val meta: Meta[ID]        = Meta[UUID].imap(apply)(_.value)

        implicit val schema: Schema[ID] =
          Schema.string[ID].description("UUID жанра музыки")
      }

      @derive(tethysReader, tethysWriter)
      @newtype final case class Name(value: String)

      object Name {
        implicit val meta: Meta[Name] = Meta[String].imap(apply)(_.value)

        implicit val schema: Schema[Name] = Schema.string[Name].description(
          "Название жанра музыки"
        )
      }
    }
  }

  @derive(tethysReader, tethysWriter)
  final case class Artist(
      id: Artist.ID,
      name: Artist.Name,
      imageUrl: Artist.ImageUrl
  )

  object Artist {
    @derive(tethysReader, tethysWriter, loggable)
    @newtype final case class ID(value: UUID)

    object ID {
      implicit val codec: PlainCodec[ID] = Codec.uuid.map(apply _)(_.value)
      implicit val meta: Meta[ID]        = Meta[UUID].imap(apply)(_.value)

      implicit val schema: Schema[ID] =
        Schema.string[ID].description("ID исполнителя")
    }

    @derive(tethysReader, tethysWriter)
    @newtype final case class Name(value: String)

    object Name {
      implicit val meta: Meta[Name] = Meta[String].imap(apply)(_.value)

      implicit val schema: Schema[Name] =
        Schema.string[Name].description("Имя исполнителя")
    }

    @derive(tethysReader, tethysWriter)
    @newtype final case class ImageUrl(value: String)

    object ImageUrl {
      implicit val meta: Meta[ImageUrl] = Meta[String].imap(apply)(_.value)

      implicit val schema: Schema[ImageUrl] =
        Schema.string[ImageUrl].description(
          "Ссылка на изображение исполнителя"
        )
    }

    implicit val schema: Schema[Artist] = Schema.derived[Artist].description(
      "Информация об исполнителе"
    )
  }

  @derive(tethysReader, tethysWriter)
  final case class Genre(
      id: Genre.ID,
      name: Genre.Name,
      imageUrl: Genre.ImageURL
  )

  object Genre {
    @derive(tethysReader, tethysWriter, loggable)
    @newtype final case class ID(value: UUID)

    object ID {
      implicit val codec: PlainCodec[ID] = Codec.uuid.map(apply _)(_.value)
      implicit val meta: Meta[ID]        = Meta[UUID].imap(apply)(_.value)

      implicit val schema: Schema[ID] =
        Schema.string[ID].description("ID жанра музыки")
    }

    @derive(tethysReader, tethysWriter)
    @newtype final case class Name(value: String)

    object Name {
      implicit val meta: Meta[Name] = Meta[String].imap(apply)(_.value)

      implicit val schema: Schema[Name] = Schema.string[Name].description(
        "Название жанра музыки"
      )
    }

    @derive(tethysReader, tethysWriter)
    @newtype final case class ImageURL(value: String)

    object ImageURL {
      implicit val meta: Meta[ImageURL] = Meta[String].imap(apply)(_.value)

      implicit val schema: Schema[ImageURL] =
        Schema.string[ImageURL].description(
          "Ссылка на изображение жанра музыки"
        )
    }

    implicit val schema: Schema[Genre] = Schema.derived[Genre].description(
      "Информация о жанре музыки"
    )
  }

  @derive(tethysReader, tethysWriter)
  final case class City(
      id: City.ID,
      name: City.Name
  )

  object City {
    @derive(tethysReader, tethysWriter, loggable)
    @newtype final case class ID(value: UUID)

    object ID {
      implicit val codec: PlainCodec[ID] = Codec.uuid.map(apply _)(_.value)
      implicit val meta: Meta[ID]        = Meta[UUID].imap(apply)(_.value)
      implicit val schema: Schema[ID] =
        Schema.string[ID].description("ID города")
    }

    @derive(tethysReader, tethysWriter)
    @newtype final case class Name(value: String)

    object Name {
      implicit val meta: Meta[Name] = Meta[String].imap(apply)(_.value)
      implicit val schema: Schema[Name] =
        Schema.string[Name].description("Название города")
    }

    implicit val schema: Schema[City] =
      Schema.derived[City].description("Город")
  }

  @derive(tethysReader, tethysWriter)
  final case class UserPurpose(
      id: UserPurpose.ID,
      name: UserPurpose.Name
  )

  object UserPurpose {
    @derive(tethysReader, tethysWriter)
    @newtype final case class ID(value: UUID)

    object ID {
      def create[F[_]: Functor: GenUUID]: F[ID] = GenUUID.random[F].map(apply)

      implicit val codec: PlainCodec[ID] = Codec.uuid.map(apply _)(_.value)
      implicit val meta: Meta[ID]        = Meta[UUID].imap(apply)(_.value)

      implicit val schema: Schema[ID] = Schema.string[ID].description(
        "ID цели использования приложения"
      )
    }

    @derive(tethysReader, tethysWriter)
    @newtype final case class Name(value: String)

    object Name {
      implicit val meta: Meta[Name] = Meta[String].imap(apply)(_.value)

      implicit val schema: Schema[Name] = Schema.string[Name].description(
        "Название цели использования приложения"
      )
    }

    implicit val schema: Schema[UserPurpose] = Schema.derived[UserPurpose]
  }

  @derive(tethysReader, tethysWriter, loggable)
  @newtype final case class Limit(value: Int)

  object Limit {
    implicit val codec: PlainCodec[Limit] = Codec.int.map(apply _)(_.value)
    implicit val meta: Meta[Limit]        = Meta[Int].imap(apply)(_.value)

    implicit val schema: Schema[Limit] = Schema.schemaForInt.map(value =>
      Some(apply(value))
    )(_.value).description(
      "Максимальное количество элементов в ответе"
    )
  }

  @derive(tethysReader, tethysWriter, loggable)
  @newtype final case class Offset(value: Int)

  object Offset {
    implicit val codec: PlainCodec[Offset] = Codec.int.map(apply _)(_.value)

    implicit val schema: Schema[Offset] = Schema.schemaForInt.map(value =>
      Some(apply(value))
    )(_.value).description(
      "Смещение первого элемента в ответе"
    )
  }
}
