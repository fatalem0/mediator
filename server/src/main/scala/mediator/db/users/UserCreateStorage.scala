package mediator.db.users

import cats.effect.MonadCancelThrow
import cats.tagless.syntax.functorK._
import derevo.derive
import derevo.tagless.applyK
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.{ ConnectionIO, Transactor }
import mediator.Domain.UserData

@derive(applyK)
private[db] trait UserCreateStorage[F[_]] {
  def create(userData: UserData): F[Boolean]
}

object UserCreateStorage {
  private object DB extends UserCreateStorage[ConnectionIO] {
    override def create(userData: UserData): ConnectionIO[Boolean] =
      sql"""
        INSERT INTO users(
          id,
          email,
          hashed_password,
          created_at,
          updated_at
        ) VALUES (
          ${userData.id},
          ${userData.email},
          ${userData.hashedPassword},
          ${userData.createdAt},
          ${userData.updatedAt}
        )
      """.update.run.map(_ > 0)
  }

  def db: UserCreateStorage[ConnectionIO] = DB

  private[db] def make[F[_]: MonadCancelThrow](
      tr: Transactor[F]
  ): UserCreateStorage[F] =
    DB.mapK(tr.trans)
}
