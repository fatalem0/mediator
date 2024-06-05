package mediator.artist

import cats.Monad
import derevo.derive
import derevo.tagless.applyK
import mediator.Domain.{ Limit, Offset }
import mediator.artist.Domain.Errors.ArtistsGetError
import mediator.artist.Domain.GetArtists
import mediator.db.artist.ArtistsGetStorage
import mediator.db.artist.Domain.Errors.GetError
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.handle._
import tofu.syntax.raise._

@derive(applyK)
trait ArtistsGetService[F[_]] {
  def get(
      limit: Limit,
      offset: Offset
  ): F[Either[ArtistsGetError, GetArtists.Response]]
}

object ArtistsGetService extends Logging.Companion[ArtistsGetService] {
  final private class Impl[F[_]: Monad: ArtistsGetError.Errors](
      storage: ArtistsGetStorage[F]
  ) extends ArtistsGetService[F] {
    override def get(
        limit: Limit,
        offset: Offset
    ): F[Either[ArtistsGetError, GetArtists.Response]] =
      storage.get(limit, offset)
        .mapIn(GetArtists.Response(offset, _))
        .catchAll[GetArtists.Response] {
          case GetError.GenresByIdNotFound(_) | GetError.GenreIdsNotFound(_) =>
            ArtistsGetError.NotFound.raise[F, GetArtists.Response]
          case GetError.PSQL(cause) =>
            ArtistsGetError.InternalDatabase(cause).raise[F, GetArtists.Response]
          case GetError.Connection(cause) =>
            ArtistsGetError.Internal(cause).raise[F, GetArtists.Response]
        }
        .attempt[ArtistsGetError]
  }

  def make[F[_]: Monad: ArtistsGetError.Errors](
      storage: ArtistsGetStorage[F]
  ): ArtistsGetService[F] = new Impl[F](storage)
}
