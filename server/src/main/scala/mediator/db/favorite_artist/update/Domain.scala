package mediator.db.favorite_artist.update

import org.postgresql.util.PSQLException
import utils.db.model.DatabaseError
import utils.db.{ SQLErrorJoiner, SQLExceptionHandler }

import java.sql.{ BatchUpdateException, SQLException }

object Domain {
  object Errors {
    sealed trait UpdateError

    object UpdateError {
      sealed abstract class ForeignKey(val constraint: String)

      object ForeignKey {
        case object UserIdNotFound
          extends ForeignKey("favorite_artists_users_user_id_fkey")

        case object FavoriteArtistIdNotFound
          extends ForeignKey("favorite_artists_users_favorite_artist_id_fkey")
      }

      final case object NoUpdate                                      extends UpdateError
      final case class ReferenceNotFound(cause: BatchUpdateException) extends UpdateError
      final case class PSQL(cause: PSQLException)                     extends UpdateError
      final case class Connection(cause: Throwable)                   extends UpdateError

      private def check(
          cause: SQLException,
          keyConstraint: ForeignKey
      ): Boolean = SQLExceptionHandler.hasForeignKeyViolation(
        cause,
        keyConstraint.constraint
      )

      def fromDatabaseError(error: DatabaseError): UpdateError =
        error match {
          case DatabaseError.Connection(th) => UpdateError.Connection(th)
          case DatabaseError.Sql(cause: BatchUpdateException)
              if check(cause, ForeignKey.FavoriteArtistIdNotFound) | check(
                cause,
                ForeignKey.UserIdNotFound
              ) =>
            UpdateError.ReferenceNotFound(cause)
          case DatabaseError.Sql(cause: PSQLException) =>
            UpdateError.PSQL(cause)
          case DatabaseError.Sql(cause) => UpdateError.Connection(cause)
        }

      implicit val errorJoiner: SQLErrorJoiner[UpdateError] =
        fromDatabaseError(_)
    }
  }
}
