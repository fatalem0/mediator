package utils.server

import cats.effect.Async
import org.http4s.HttpApp
import sttp.tapir.server.http4s.{ Http4sServerInterpreter, Http4sServerOptions }

trait HttpMakeRoute[F[_], Route] {
  def makeRoute(
      httpModule: HttpModule[F]
  ): Route
}

object HttpMakeRoute {
  type Http4s[F[_]] = HttpMakeRoute[F, HttpApp[F]]

  final private class Http4sImpl[F[_]: Async](options: Http4sServerOptions[F])
    extends HttpMakeRoute[F, HttpApp[F]] {
    implicit val interpreter: Http4sServerInterpreter[F] =
      Http4sServerInterpreter[F](options)

    override def makeRoute(httpModule: HttpModule[F]): HttpApp[F] =
      interpreter.toRoutes(httpModule).orNotFound
  }

  def make[F[_]: Async](options: Http4sServerOptions[F]): Http4s[F] =
    new Http4sImpl[F](options)
}
