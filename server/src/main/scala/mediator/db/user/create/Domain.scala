package mediator.db.user.create

import mediator.Domain.User
import org.postgresql.util.PSQLException
import utils.db.{ PSQLExceptionHandler, SQLErrorJoiner }
import utils.db.model.DatabaseError

import java.time.Instant

object Domain {
  object Errors {
    sealed trait CreateError

    object CreateError {
      sealed abstract class KeyConstraint(val constraint: String)

      object KeyConstraint {
        case object UserEmail extends KeyConstraint("user_email_ext_key")
      }

      final case object NoUpdate                                extends CreateError
      final case class AlreadyExists(constraint: KeyConstraint) extends CreateError
      final case class PSQL(cause: PSQLException)               extends CreateError
      final case class Connection(cause: Throwable)             extends CreateError

      private def check(
          cause: PSQLException,
          keyConstraint: KeyConstraint
      ): Boolean = PSQLExceptionHandler.hasConstraint(
        cause,
        keyConstraint.constraint
      )

      def fromDatabaseError(error: DatabaseError): CreateError =
        error match {
          case DatabaseError.Connection(th) => CreateError.Connection(th)
          case DatabaseError.Sql(cause: PSQLException)
              if check(cause, KeyConstraint.UserEmail) =>
            CreateError.AlreadyExists(KeyConstraint.UserEmail)
          case DatabaseError.Sql(cause: PSQLException) =>
            CreateError.PSQL(cause)
          case DatabaseError.Sql(cause) => CreateError.Connection(cause)
        }

      implicit val errorJoiner: SQLErrorJoiner[CreateError] =
        fromDatabaseError(_)
    }
  }

  final case class CreateUserRow(
      id: User.ID,
      email: User.Email,
      hashedPassword: User.Password,
      createdAt: Instant,
      updatedAt: Instant
  )
}
