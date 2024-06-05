package mediator.db.artist

import derevo.derive
import derevo.tethys.{ tethysReader, tethysWriter }
import mediator.Domain.{ Artist, Genre }
import org.postgresql.util.PSQLException
import sttp.tapir.Schema
import utils.db.SQLErrorJoiner
import utils.db.model.DatabaseError

import scala.util.control.NoStackTrace

object Domain {
  object Errors {
    sealed trait GetError

    object GetError {
      final case class GenreIdsNotFound(artistIds: Vector[Artist.ID]) extends GetError
      final case class GenresByIdNotFound(genreIds: Vector[Genre.ID]) extends GetError
      final case class PSQL(cause: PSQLException)                     extends GetError
      final case class Connection(cause: Throwable)                   extends GetError

      sealed trait InternalErrors extends GetError

      object InternalErrors {
        final private[artist] case class GenreIdsNotFound(
            artistIds: Vector[Artist.ID]
        ) extends Throwable("Can't find genre IDs for artist IDs")
            with InternalErrors
            with NoStackTrace

        final private[artist] case class GenresByIdNotFound(
            genreIds: Vector[Genre.ID]
        ) extends Throwable("Can't find genre ids by their ID's")
            with InternalErrors
            with NoStackTrace
      }

      def fromDatabaseError(error: DatabaseError): GetError =
        error match {
          case DatabaseError.Connection(InternalErrors.GenreIdsNotFound(artistIds)) =>
            GetError.GenreIdsNotFound(artistIds)
          case DatabaseError.Connection(InternalErrors.GenresByIdNotFound(genreIds)) =>
            GetError.GenresByIdNotFound(genreIds)
          case DatabaseError.Connection(th)            => GetError.Connection(th)
          case DatabaseError.Sql(cause: PSQLException) => GetError.PSQL(cause)
          case DatabaseError.Sql(cause)                => GetError.Connection(cause)
        }

      implicit val errorJoiner: SQLErrorJoiner[GetError] = fromDatabaseError(_)
    }
  }

  @derive(tethysReader, tethysWriter)
  final case class ArtistWithGenre(
      id: Artist.ID,
      name: Artist.Name,
      genre: Genre.Name,
      imageUrl: Artist.ImageUrl
  )

  object ArtistWithGenre {
    implicit val schema: Schema[ArtistWithGenre] = Schema.derived
  }
}
