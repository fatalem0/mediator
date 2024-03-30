package utils.server

import cats.Functor
import sttp.tapir.Endpoint
import sttp.tapir.server.ServerEndpoint
import tofu.syntax.feither._
import utils.errors.ApiError
import utils.errors.ApiError.Descriptor

trait ApiBuilder[F[_]] {
  def build[INPUT, ERROR_OUT, OUT](
      logic: INPUT => F[Either[ERROR_OUT, OUT]],
      endpoint: Endpoint[Unit, INPUT, ApiError, OUT, Any]
  )(implicit descriptor: Descriptor[ERROR_OUT]): ServerEndpoint[Any, F]
}

object ApiBuilder {
  final private class Impl[F[_]: Functor] extends ApiBuilder[F] {
    override def build[INPUT, ERROR_OUT, OUT](
        logic: INPUT => F[Either[ERROR_OUT, OUT]],
        endpoint: Endpoint[Unit, INPUT, ApiError, OUT, Any]
    )(implicit descriptor: Descriptor[ERROR_OUT]): ServerEndpoint[Any, F] =
      endpoint
        .serverLogic(
          logic(_).leftMapIn[ApiError](e =>
            ApiError(descriptor.code(e), descriptor.message(e))
          )
        )
  }

  def make[F[_]: Functor]: ApiBuilder[F] =
    new Impl[F]
}
