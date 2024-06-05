package mediator.db.city

import org.postgresql.util.PSQLException
import utils.db.SQLErrorJoiner
import utils.db.model.DatabaseError

object Domain {
  object Errors {
    sealed trait GetError

    object GetError {
      case object NotFound                          extends GetError
      final case class PSQL(cause: PSQLException)   extends GetError
      final case class Connection(cause: Throwable) extends GetError

      def fromDatabaseError(error: DatabaseError): GetError =
        error match {
          case DatabaseError.Connection(th)            => GetError.Connection(th)
          case DatabaseError.Sql(cause: PSQLException) => GetError.PSQL(cause)
          case DatabaseError.Sql(cause)                => GetError.Connection(cause)
        }

      implicit val errorJoiner: SQLErrorJoiner[GetError] = fromDatabaseError(_)
    }
  }
}
