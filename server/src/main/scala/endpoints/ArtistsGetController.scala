package endpoints

import mediator.Domain.{ Limit, Offset }
import mediator.artist.ArtistsGetService
import mediator.artist.Domain.Errors.ArtistsGetError
import mediator.artist.Domain.GetArtists
import sttp.tapir._
import sttp.tapir.json.tethysjson.jsonBody
import utils.errors.ApiError
import utils.server.{ ApiBuilder, HttpModule, WireWithLogic }

trait ArtistsGetController[F[_]] {
  def get(
      limit: Limit,
      offset: Offset
  ): F[Either[ArtistsGetError, GetArtists.Response]]
}

object ArtistsGetController {
  final private class Impl[F[_]](service: ArtistsGetService[F])
    extends ArtistsGetController[F] {
    override def get(
        limit: Limit,
        offset: Offset
    ): F[Either[ArtistsGetError, GetArtists.Response]] = service.get(
      limit,
      offset
    )
  }

  object protocol {
    val update: Endpoint[
      Unit,
      (Limit, Offset),
      ApiError,
      GetArtists.Response,
      Any
    ] = endpoint
      .summary("Получение исполнителей")
      .get
      .in(ApiV1 / "artists")
      .in(query[Limit]("limit").description(
        "Максимальное количество элементов в ответе"
      ))
      .in(query[Offset]("offset").description(
        "Смещение первого элемента в ответе"
      ))
      .errorOut(
        ApiError.makeOneOf[ArtistsGetError](ArtistsGetError.variants)
      )
      .out(jsonBody[GetArtists.Response])
  }

  implicit val wireWithLogic: WireWithLogic[ArtistsGetController] =
    new WireWithLogic[ArtistsGetController] {
      override def wire[F[_]](controller: ArtistsGetController[F])(implicit
          builder: ApiBuilder[F]
      ): HttpModule[F] = List(
        builder.build(
          (controller.get _).tupled,
          protocol.update
        )
      )
    }

  def make[F[_]](service: ArtistsGetService[F]): ArtistsGetController[F] =
    new Impl[F](service)
}
