package mediator.db.user.update

import mediator.Domain.{ City, User }
import org.postgresql.util.PSQLException
import utils.db.SQLErrorJoiner
import utils.db.model.DatabaseError

import java.time.Instant

object Domain {
  object Errors {
    sealed trait UpdateError

    object UpdateError {
      final case object NoUpdate                    extends UpdateError
      final case class PSQL(cause: PSQLException)   extends UpdateError
      final case class Connection(cause: Throwable) extends UpdateError

      def fromDatabaseError(error: DatabaseError): UpdateError =
        error match {
          case DatabaseError.Connection(th) => UpdateError.Connection(th)
          case DatabaseError.Sql(cause: PSQLException) =>
            UpdateError.PSQL(cause)
          case DatabaseError.Sql(cause) => UpdateError.Connection(cause)
        }

      implicit val errorJoiner: SQLErrorJoiner[UpdateError] =
        fromDatabaseError(_)
    }
  }

  final case class UpdateUserRow(
      email: Option[User.Email],
      hashedPassword: Option[User.Password],
      updatedAt: Instant,
      accountName: Option[User.AccountName],
      imageURL: Option[User.ImageURL],
      about: Option[User.About],
      city: Option[City.ID]
  )
}
