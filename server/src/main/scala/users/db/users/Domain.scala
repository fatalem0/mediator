package users.db.users

import org.postgresql.util.PSQLException
import utils.db.{ PSQLExceptionHandler, SQLErrorJoiner }
import utils.db.model.DatabaseError

object Domain {
  object Errors {
    sealed trait ReadError

    object ReadError {
      case object NotFound                          extends ReadError
      final case class PSQL(cause: PSQLException)   extends ReadError
      final case class Connection(cause: Throwable) extends ReadError

      def notFound: ReadError = NotFound

      def fromDatabaseError(error: DatabaseError): ReadError =
        error match {
          case DatabaseError.Connection(th) => ReadError.Connection(th)
          case DatabaseError.Sql(cause: PSQLException) => ReadError.PSQL(cause)
          case DatabaseError.Sql(cause) => ReadError.Connection(cause)
        }

      implicit val errorJoiner: SQLErrorJoiner[ReadError] =
        fromDatabaseError(_)
    }

    sealed trait CreateError

    object CreateError {
      sealed abstract class KeyConstraint(val constraint: String)

      object KeyConstraint {
        case object UserEmail extends KeyConstraint("user_email_ext_key")
      }

      final case object NoUpdate extends CreateError
      final case class AlreadyExists(constraint: KeyConstraint)
        extends CreateError
      final case class PSQL(cause: PSQLException)   extends CreateError
      final case class Connection(cause: Throwable) extends CreateError

      private def check(
          cause: PSQLException,
          keyConstraint: KeyConstraint
      ): Boolean =
        PSQLExceptionHandler.hasConstraint(cause, keyConstraint.constraint)

      def fromDatabaseError(error: DatabaseError): CreateError =
        error match {
          case DatabaseError.Connection(th) =>
            CreateError.Connection(th)
          case DatabaseError.Sql(cause: PSQLException)
              if check(cause, KeyConstraint.UserEmail) =>
            CreateError.AlreadyExists(KeyConstraint.UserEmail)
          case DatabaseError.Sql(cause: PSQLException) =>
            CreateError.PSQL(cause)
          case DatabaseError.Sql(cause) =>
            CreateError.Connection(cause)
        }

      implicit val errorJoiner: SQLErrorJoiner[CreateError] =
        fromDatabaseError(_)
    }

    sealed trait CheckError

    object CheckError {
      final case class PSQL(cause: PSQLException)   extends CheckError
      final case class Connection(cause: Throwable) extends CheckError

      def fromDatabaseError(error: DatabaseError): CheckError =
        error match {
          case DatabaseError.Connection(th) => CheckError.Connection(th)
          case DatabaseError.Sql(cause: PSQLException) => CheckError.PSQL(cause)
          case DatabaseError.Sql(cause) => CheckError.Connection(cause)
        }

      implicit val errorJoiner: SQLErrorJoiner[CheckError] =
        fromDatabaseError(_)
    }
  }
}
