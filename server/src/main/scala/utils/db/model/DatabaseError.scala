package utils.db.model

import java.sql.SQLException

sealed trait DatabaseError

object DatabaseError {
  final case class Connection(th: Throwable) extends DatabaseError
  final case class Sql(cause: SQLException)  extends DatabaseError

  def fromThrowable(th: Throwable): DatabaseError =
    th match {
      case sql: java.sql.SQLException => DatabaseError.Sql(sql)
      case th: Throwable              => DatabaseError.Connection(th)
    }
}
