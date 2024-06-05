package mediator.db.chat.check

import org.postgresql.util.PSQLException
import utils.db.SQLErrorJoiner
import utils.db.model.DatabaseError

object Domain {
  object Errors {
    sealed trait CheckError

    object CheckError {
      final case class PSQL(cause: PSQLException)   extends CheckError
      final case class Connection(cause: Throwable) extends CheckError

      def fromDatabaseError(error: DatabaseError): CheckError =
        error match {
          case DatabaseError.Connection(th)            => CheckError.Connection(th)
          case DatabaseError.Sql(cause: PSQLException) => CheckError.PSQL(cause)
          case DatabaseError.Sql(cause)                => CheckError.Connection(cause)
        }

      implicit val errorJoiner: SQLErrorJoiner[CheckError] =
        fromDatabaseError(_)
    }
  }
}
