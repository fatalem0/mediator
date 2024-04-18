package mediator.db.users

import cats.effect.MonadCancelThrow
import cats.tagless.syntax.functorK._
import derevo.derive
import derevo.tagless.applyK
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.{ ConnectionIO, Transactor }
import tofu.logging.Logging
import mediator.Domain.{ UserData, UserEmail }

@derive(applyK)
private[db] trait UserReadStorage[F[_]] {
  def findByEmail(email: UserEmail): F[Option[UserData]]
}

object UserReadStorage extends Logging.Companion[UserReadStorage] {
  final private object DB extends UserReadStorage[ConnectionIO] {
    override def findByEmail(email: UserEmail): ConnectionIO[Option[UserData]] =
      sql"""
        $selectFragment
        WHERE email = $email
      """.query[UserData].option

    private val selectFragment =
      fr"""
        SELECT
          id,
          email,
          hashed_password,
          created_at,
          updated_at
        FROM users
      """
  }

  def db: UserReadStorage[ConnectionIO] = DB

  def make[F[_]: MonadCancelThrow](tr: Transactor[F]): UserReadStorage[F] =
    DB.mapK(tr.trans)
}
