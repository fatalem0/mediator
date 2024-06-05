package endpoints

import cats.Applicative
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.{ endpoint, Endpoint }
import mediator.login.Domain.Errors.LoginError
import mediator.login.Domain.Login
import mediator.login.LoginService
import utils.errors.ApiError
import utils.server.{ ApiBuilder, HttpModule, WireWithLogic }

trait LoginController[F[_]] {
  def login(req: Login.Request): F[Either[LoginError, Login.Response]]
}

object LoginController {
  final private class Impl[F[_]: Applicative](service: LoginService[F])
    extends LoginController[F] {
    override def login(req: Login.Request): F[Either[
      LoginError,
      Login.Response
    ]] = service.login(req)
  }

  object protocol {
    val login: Endpoint[
      Unit,
      Login.Request,
      ApiError,
      Login.Response,
      Any
    ] = endpoint
      .summary("Аутентификация пользователя")
      .post
      .in(ApiV1 / "login")
      .in(jsonBody[Login.Request])
      .errorOut(
        ApiError.makeOneOf[LoginError](LoginError.variants)
      )
      .out(jsonBody[Login.Response])
  }

  implicit val wireWithLogic: WireWithLogic[LoginController] =
    new WireWithLogic[LoginController] {
      override def wire[F[_]](controller: LoginController[F])(implicit
          builder: ApiBuilder[F]
      ): HttpModule[F] = List(
        builder.build(
          controller.login,
          protocol.login
        )
      )
    }

  def make[F[_]: Applicative](service: LoginService[F]): LoginController[F] =
    new Impl[F](service)
}
