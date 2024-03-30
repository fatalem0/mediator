package wiring

import cats.effect.{ Async, Clock }
import tofu.generate.GenUUID
import users.services.auth.AuthService
import users.services.registration.RegistrationService
import users.services.users.UserService

class ServiceComponent[F[_]](implicit
    val auth: AuthService[F],
    val user: UserService[F],
    val registration: RegistrationService[F]
)

object ServiceComponent {
  def make[I[_]: Async: Clock: GenUUID](
      core: CoreComponent[I],
      db: DatabaseComponent[I]
  ): ServiceComponent[I] = {
    import core._

    implicit val authService: AuthService[I] =
      AuthService.make[I](conf.auth.ttl)
    implicit val userService: UserService[I] =
      UserService.make[I](db.userStorage)

    implicit val registration: RegistrationService[I] =
      RegistrationService.makeObservable[I](authService, userService)

    new ServiceComponent[I]
  }
}
