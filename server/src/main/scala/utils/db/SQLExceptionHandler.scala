package utils.db

import doobie.postgres.sqlstate.class23

import java.sql.SQLException

object SQLExceptionHandler {
  def hasForeignKeyViolation(
      cause: SQLException,
      constraintName: String
  ): Boolean =
    cause.getSQLState == class23.FOREIGN_KEY_VIOLATION.value && cause.getMessage.contains(
      constraintName
    )
}
