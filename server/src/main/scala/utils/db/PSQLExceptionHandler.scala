package utils.db

import doobie.postgres.sqlstate.class23
import org.postgresql.util.PSQLException

object PSQLExceptionHandler {
  def hasConstraint(
      cause: PSQLException,
      constraintName: String
  ): Boolean =
    (for {
      serverErrorMessage <- Option(cause.getServerErrorMessage)
      sqlState           <- Option(serverErrorMessage.getSQLState)
      constraint         <- Option(serverErrorMessage.getConstraint)
    } yield sqlState == class23.UNIQUE_VIOLATION.value && constraint == constraintName)
      .getOrElse(false)
}
