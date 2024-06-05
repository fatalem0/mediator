package endpoints

import mediator.Domain.Genre
import mediator.genre.Domain.Errors.GenresGetError
import mediator.genre.GenresGetService
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.{ endpoint, Endpoint }
import utils.errors.ApiError
import utils.server.{ ApiBuilder, HttpModule, WireWithLogic }

trait GenresGetController[F[_]] {
  def getGenres: F[Either[GenresGetError, Vector[Genre]]]
}

object GenresGetController {
  final private class Impl[F[_]](service: GenresGetService[F])
    extends GenresGetController[F] {
    override def getGenres: F[Either[GenresGetError, Vector[Genre]]] =
      service.getGenres
  }

  object protocol {
    val get: Endpoint[
      Unit,
      Unit,
      ApiError,
      Vector[Genre],
      Any
    ] = endpoint
      .summary("Получение списка жанров музыки")
      .get
      .in(ApiV1 / "genres")
      .errorOut(
        ApiError.makeOneOf[GenresGetError](GenresGetError.variants)
      )
      .out(jsonBody[Vector[Genre]])
  }

  implicit val wireWithLogic: WireWithLogic[GenresGetController] =
    new WireWithLogic[GenresGetController] {
      override def wire[F[_]](controller: GenresGetController[F])(implicit
          builder: ApiBuilder[F]
      ): HttpModule[F] = List(
        builder.build[Unit, GenresGetError, Vector[Genre]](
          _ => controller.getGenres,
          protocol.get
        )
      )
    }

  def make[F[_]](service: GenresGetService[F]): GenresGetController[F] =
    new Impl[F](service)
}
