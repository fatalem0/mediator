package utils.server

import cats.effect.{ Async, Resource }
import configs.ServerConf
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

object Http4sServer {
  def buildEmber[F[_]: Async](
      httpApp: HttpApp[F],
      config: ServerConf
  ): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withHost(config.host)
      .withPort(config.port)
      .withHttpApp(httpApp)
      .withShutdownTimeout(config.shutdownTimeout)
      .build
}
