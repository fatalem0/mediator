package endpoints

import mediator.Domain.City
import mediator.city.CitiesGetService
import mediator.city.Domain.Errors.CitiesGetError
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.{ endpoint, Endpoint }
import utils.errors.ApiError
import utils.server.{ ApiBuilder, HttpModule, WireWithLogic }

trait CitiesGetController[F[_]] {
  def getCities: F[Either[CitiesGetError, Vector[City]]]
}

object CitiesGetController {
  final private class Impl[F[_]](service: CitiesGetService[F])
    extends CitiesGetController[F] {
    override def getCities: F[Either[CitiesGetError, Vector[City]]] =
      service.getCities
  }

  object protocol {
    val get: Endpoint[
      Unit,
      Unit,
      ApiError,
      Vector[City],
      Any
    ] = endpoint
      .summary("Получение списка городов России")
      .get
      .in(ApiV1 / "cities")
      .errorOut(
        ApiError.makeOneOf[CitiesGetError](CitiesGetError.variants)
      )
      .out(jsonBody[Vector[City]])
  }

  implicit val wireWithLogic: WireWithLogic[CitiesGetController] =
    new WireWithLogic[CitiesGetController] {
      override def wire[F[_]](controller: CitiesGetController[F])(implicit
          builder: ApiBuilder[F]
      ): HttpModule[F] = List(
        builder.build[Unit, CitiesGetError, Vector[City]](
          _ => controller.getCities,
          protocol.get
        )
      )
    }

  def make[F[_]](service: CitiesGetService[F]): CitiesGetController[F] =
    new Impl[F](service)
}
