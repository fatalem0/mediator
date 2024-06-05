package mediator._stubs

import cats.effect.IO
import mediator.Domain.{ Limit, Offset }
import mediator.db.artist.ArtistsGetStorage
import mediator.db.artist.Domain.ArtistWithGenre
import mediator.db.artist.Domain.Errors.GetError

final class ArtistsGetStorageStub(
    response: Either[GetError, Vector[ArtistWithGenre]]
) extends ArtistsGetStorage[IO] {
  override def get(
      limit: Limit,
      offset: Offset
  ): IO[Either[GetError, Vector[ArtistWithGenre]]] = IO.pure(response)
}
