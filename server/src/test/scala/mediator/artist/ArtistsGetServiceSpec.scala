package mediator.artist

import cats.effect.unsafe.implicits.global
import mediator.Domain.{ Artist, Genre, Limit, Offset }
import mediator._stubs.ArtistsGetStorageStub
import mediator.artist.Domain.Errors.ArtistsGetError
import mediator.artist.Domain.GetArtists
import mediator.db.artist.Domain.ArtistWithGenre
import mediator.db.artist.Domain.Errors.GetError
import org.postgresql.util.{ PSQLException, PSQLState }
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.errors.ErrorLevel

import java.util.UUID

class ArtistsGetServiceSpec extends AnyFlatSpec with Matchers
    with EitherValues {
  import ArtistsGetServiceSpec._

  "get" should "successfully return artists" in {
    val artists = Vector(
      ArtistWithGenre(
        id = Artist.ID(UUID.randomUUID()),
        name = Artist.Name("Nirvana"),
        genre = Genre.Name("Рок"),
        imageUrl = Artist.ImageUrl("URL")
      )
    )

    val response = GetArtists.Response(
      offsetSample,
      artists
    )

    val artistsGetStorage = new ArtistsGetStorageStub(Right(artists))
    val artistsGetService = ArtistsGetService.make(artistsGetStorage)

    artistsGetService.get(
      limitSample,
      offsetSample
    ).unsafeRunSync().value shouldBe response
  }

  it should "return NotFound error if genre ids for artist ids hasn't been found" in {
    val storageErrorResponse = Left(
      GetError.GenreIdsNotFound(Vector(Artist.ID(UUID.randomUUID())))
    )
    val artistsGetStorage = new ArtistsGetStorageStub(storageErrorResponse)
    val artistsGetService = ArtistsGetService.make(artistsGetStorage)

    artistsGetService
      .get(limitSample, offsetSample)
      .unsafeRunSync().left.value shouldBe a[ArtistsGetError.NotFound.type]

    artistsGetService
      .get(limitSample, offsetSample)
      .unsafeRunSync().left.value.code shouldBe "ARTISTS_NOT_FOUND"

    artistsGetService
      .get(limitSample, offsetSample)
      .unsafeRunSync().left.value.level shouldBe ErrorLevel.NotFound
  }

  it should "return NotFound error if genres hasn't been found by their ids" in {
    val storageErrorResponse = Left(
      GetError.GenresByIdNotFound(Vector(Genre.ID(UUID.randomUUID())))
    )
    val artistsGetStorage = new ArtistsGetStorageStub(storageErrorResponse)
    val artistsGetService = ArtistsGetService.make(artistsGetStorage)

    artistsGetService
      .get(limitSample, offsetSample)
      .unsafeRunSync().left.value shouldBe a[ArtistsGetError.NotFound.type]

    artistsGetService
      .get(limitSample, offsetSample)
      .unsafeRunSync().left.value.code shouldBe "ARTISTS_NOT_FOUND"

    artistsGetService
      .get(limitSample, offsetSample)
      .unsafeRunSync().left.value.level shouldBe ErrorLevel.NotFound
  }

  it should "return InternalDatabase error" in {
    val storageErrorResponse = Left(GetError.PSQL(new PSQLException(
      "ERROR",
      PSQLState.SYSTEM_ERROR
    )))
    val artistsGetStorage = new ArtistsGetStorageStub(storageErrorResponse)
    val artistsGetService = ArtistsGetService.make(artistsGetStorage)

    artistsGetService
      .get(limitSample, offsetSample)
      .unsafeRunSync().left.value shouldBe a[ArtistsGetError.InternalDatabase]

    artistsGetService
      .get(limitSample, offsetSample)
      .unsafeRunSync().left.value.code shouldBe "ARTISTS_GET_INTERNAL_DATABASE"

    artistsGetService
      .get(limitSample, offsetSample)
      .unsafeRunSync().left.value.level shouldBe ErrorLevel.Internal
  }

  it should "return Internal error" in {
    val storageErrorResponse = Left(
      GetError.Connection(new NullPointerException())
    )
    val artistsGetStorage = new ArtistsGetStorageStub(storageErrorResponse)
    val artistsGetService = ArtistsGetService.make(artistsGetStorage)

    artistsGetService
      .get(limitSample, offsetSample)
      .unsafeRunSync().left.value shouldBe a[ArtistsGetError.Internal]

    artistsGetService
      .get(limitSample, offsetSample)
      .unsafeRunSync().left.value.code shouldBe "ARTISTS_GET_INTERNAL"

    artistsGetService
      .get(limitSample, offsetSample)
      .unsafeRunSync().left.value.level shouldBe ErrorLevel.Internal
  }
}

object ArtistsGetServiceSpec {
  val limitSample: Limit   = Limit(6)
  val offsetSample: Offset = Offset(0)
}
