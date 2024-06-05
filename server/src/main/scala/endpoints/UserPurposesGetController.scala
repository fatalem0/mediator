package endpoints

import mediator.Domain.UserPurpose
import mediator.user_purpose.get.Domain.Errors.UserPurposesGetError
import mediator.user_purpose.get.UserPurposesGetService
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.{ endpoint, Endpoint }
import utils.errors.ApiError
import utils.server.{ ApiBuilder, HttpModule, WireWithLogic }

trait UserPurposesGetController[F[_]] {
  def getUserPurposes: F[Either[UserPurposesGetError, Vector[UserPurpose]]]
}

object UserPurposesGetController {
  final private class Impl[F[_]](service: UserPurposesGetService[F])
    extends UserPurposesGetController[F] {
    override def getUserPurposes: F[Either[
      UserPurposesGetError,
      Vector[UserPurpose]
    ]] =
      service.getUserPurposes
  }

  object protocol {
    val get: Endpoint[
      Unit,
      Unit,
      ApiError,
      Vector[UserPurpose],
      Any
    ] = endpoint
      .summary("Получение списка целей использования приложения")
      .get
      .in(ApiV1 / "user-purposes")
      .errorOut(
        ApiError.makeOneOf[UserPurposesGetError](
          UserPurposesGetError.variants
        )
      )
      .out(jsonBody[Vector[UserPurpose]])
  }

  implicit val wireWithLogic: WireWithLogic[UserPurposesGetController] =
    new WireWithLogic[UserPurposesGetController] {
      override def wire[F[_]](controller: UserPurposesGetController[F])(implicit
          builder: ApiBuilder[F]
      ): HttpModule[F] = List(
        builder.build[Unit, UserPurposesGetError, Vector[UserPurpose]](
          _ => controller.getUserPurposes,
          protocol.get
        )
      )
    }

  def make[F[_]](
      service: UserPurposesGetService[F]
  ): UserPurposesGetController[F] = new Impl[F](service)
}
