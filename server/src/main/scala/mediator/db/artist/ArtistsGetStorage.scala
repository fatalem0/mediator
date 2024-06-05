package mediator.db.artist

import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import cats.{ FlatMap, MonadThrow }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.Domain.{ Limit, Offset }
import mediator.db.artist.Domain.ArtistWithGenre
import mediator.db.artist.Domain.Errors.GetError
import mediator.db.genre.GenresStorage
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.foption._
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait ArtistsGetStorage[F[_]] {
  def get(
      limit: Limit,
      offset: Offset
  ): F[Either[GetError, Vector[ArtistWithGenre]]]
}

object ArtistsGetStorage extends Logging.Companion[ArtistsGetStorage] {
  final private class Impl[F[_]: MonadThrow](
      artistsStorage: ArtistsStorage[F],
      genresStorage: GenresStorage[F]
  ) extends ArtistsGetStorage[F] {
    override def get(
        limit: Limit,
        offset: Offset
    ): F[Either[GetError, Vector[ArtistWithGenre]]] = {
      for {
        artistRows <- artistsStorage.get(limit, offset)
        artistIds = artistRows.map(_.id)

        genreIds <-
          artistIds
            .traverse(genresStorage.getByArtistID)
            .map(_.sequence)
            .toRightIn(GetError.InternalErrors.GenreIdsNotFound(artistIds))
            .reRaise

        genres <-
          genreIds
            .traverse(genresStorage.getById)
            .map(_.sequence)
            .toRightIn(GetError.InternalErrors.GenresByIdNotFound(genreIds))
            .reRaise

        res = artistRows.zip(genres.map(_.name)).map {
          case (artistRow, genreName) =>
            ArtistWithGenre(
              artistRow.id,
              artistRow.name,
              genreName,
              artistRow.imageUrl
            )
        }
      } yield Right(res)
    }
  }

  final private class LogMid[F[_]: FlatMap: ArtistsGetStorage.Log]
    extends ArtistsGetStorage[Mid[F, *]] {
    override def get(
        limit: Limit,
        offset: Offset
    ): Mid[F, Either[GetError, Vector[ArtistWithGenre]]] =
      debug"Trying to get artists with limit = $limit and offset = $offset" *>
        _.flatTap {
          case Left(GetError.GenreIdsNotFound(artistIds)) =>
            warn"Can't find genre IDs for artist IDs: $artistIds"
          case Left(GetError.GenresByIdNotFound(genreIds)) =>
            warn"Can't find genre ids by their ID's: $genreIds"
          case Left(GetError.PSQL(cause)) =>
            errorCause"Failed to get artist list with limit = $limit and offset = $offset" (
              cause
            )
          case Left(GetError.Connection(cause)) =>
            errorCause"Failed to get artist list with limit = $limit and offset = $offset due to connection error" (
              cause
            )
          case Right(_) =>
            debug"Successfully got artist list with limit = $limit and offset = $offset"
        }
  }

  private object Errors extends ArtistsGetStorage[SQLErrorJoiner] {
    override def get(
        limit: Limit,
        offset: Offset
    ): SQLErrorJoiner[Either[GetError, Vector[ArtistWithGenre]]] =
      SQLErrorJoiner[Either[GetError, Vector[ArtistWithGenre]]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: ArtistsGetStorage[F] =
    DatabaseRunner[ArtistsGetStorage, F].wire(
      new Impl[ConnectionIO](
        ArtistsStorage.db,
        GenresStorage.db
      ),
      Errors
    )

  def makeObservable[
      F[_]: MonadCancelThrow: SafeTransactor: Logging.Make
  ]: ArtistsGetStorage[F] = {
    val logMid = new LogMid[F]: ArtistsGetStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
