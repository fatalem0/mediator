package wiring

import endpoints.RegistrationController
import utils.server._

class PublicControllers[F[_]](implicit
    val registration: RegistrationController[F]
)

object PublicControllers {
  implicit val wire: WireWithLogic[PublicControllers] =
    new WireWithLogic[PublicControllers] {
      override def wire[F[_]](controllers: PublicControllers[F])(implicit
          builder: ApiBuilder[F]
      ): HttpModule[F] = {
        import controllers._

        registration.wire
      }
    }

  def make[F[_]](
      services: ServiceComponent[F]
  ): PublicControllers[F] = {
    implicit val registrationController: RegistrationController[F] =
      RegistrationController.make[F](services.registration)

    new PublicControllers[F]
  }
}
