package endpoints

import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.{ endpoint, Endpoint }
import mediator.services.registration.Domain.Errors.RegistrationError
import mediator.services.registration.Domain.Registration
import mediator.services.registration.RegistrationService
import utils.errors.ApiError
import utils.server.{ ApiBuilder, HttpModule, WireWithLogic }

trait RegistrationController[F[_]] {
  def register(
      req: Registration.Request
  ): F[Either[RegistrationError, Registration.Response]]
}

object RegistrationController {
  final private class Impl[F[_]](service: RegistrationService[F])
    extends RegistrationController[F] {
    override def register(
        req: Registration.Request
    ): F[Either[RegistrationError, Registration.Response]] =
      service.register(req)
  }

  object protocol {
    val register: Endpoint[
      Unit,
      Registration.Request,
      ApiError,
      Registration.Response,
      Any
    ] =
      endpoint
        .summary("Регистрация пользователя")
        .post
        .in(ApiV1 / "register")
        .in(jsonBody[Registration.Request])
        .errorOut(
          ApiError.makeOneOf[RegistrationError](RegistrationError.variants)
        )
        .out(jsonBody[Registration.Response])
  }

  implicit val wireWithLogic: WireWithLogic[RegistrationController] =
    new WireWithLogic[RegistrationController] {
      override def wire[F[_]](controller: RegistrationController[F])(implicit
          builder: ApiBuilder[F]
      ): HttpModule[F] =
        List(
          builder.build(
            controller.register,
            protocol.register
          )
        )
    }

  def make[F[_]](
      service: RegistrationService[F]
  ): RegistrationController[F] =
    new Impl[F](service)
}
