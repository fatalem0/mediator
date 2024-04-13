package wiring

import cats.Applicative
import endpoints.{ LoginController, RegistrationController }
import utils.server._

class PublicControllers[F[_]](implicit
    val registration: RegistrationController[F],
    val login: LoginController[F]
)

object PublicControllers {
  implicit val wire: WireWithLogic[PublicControllers] =
    new WireWithLogic[PublicControllers] {
      override def wire[F[_]](controllers: PublicControllers[F])(implicit
          builder: ApiBuilder[F]
      ): HttpModule[F] = {
        import controllers._

        registration.wire ++
          login.wire
      }
    }

  def make[F[_]: Applicative](
      services: ServiceComponent[F]
  ): PublicControllers[F] = {
    implicit val registrationController: RegistrationController[F] =
      RegistrationController.make[F](services.registration)

    implicit val loginController: LoginController[F] =
      LoginController.make[F](services.login)

    new PublicControllers[F]
  }
}
