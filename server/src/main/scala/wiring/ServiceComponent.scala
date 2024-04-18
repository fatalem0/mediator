package wiring

import cats.effect.{ Async, Clock }
import tofu.generate.GenUUID
import mediator.services.token.TokenService
import mediator.services.login.LoginService
import mediator.services.registration.RegistrationService
import mediator.services.users.UserService

class ServiceComponent[F[_]](implicit
    val auth: TokenService[F],
    val user: UserService[F],
    val registration: RegistrationService[F],
    val login: LoginService[F]
)

object ServiceComponent {
  def make[I[_]: Async: Clock: GenUUID](
      core: CoreComponent[I],
      db: DatabaseComponent[I]
  ): ServiceComponent[I] = {
    import core._

    implicit val tokenService: TokenService[I] =
      TokenService.make[I](conf.auth.ttl)
    implicit val userService: UserService[I] =
      UserService.make[I](db.userStorage)

    implicit val registration: RegistrationService[I] =
      RegistrationService.makeObservable[I](tokenService, userService)

    implicit val login: LoginService[I] =
      LoginService.makeObservable[I](db.userStorage, tokenService)

    new ServiceComponent[I]
  }
}
