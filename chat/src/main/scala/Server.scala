import cats.effect.kernel.Async
import cats.effect.std.Queue
import com.comcast.ip4s._
import fs2.concurrent.Topic
import fs2.io.file.Files
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.websocket.WebSocketFrame

object Server {
  def server[F[_]: Async: Files: Network](
      q: Queue[F, WebSocketFrame],
      t: Topic[F, WebSocketFrame]
  ): F[Nothing] =
    EmberServerBuilder
      .default[F]
      .withHost(host"0.0.0.0")
      .withPort(port"8080")
      .withHttpWebSocketApp(wsb => new Routes().service(wsb, q, t))
      .build
      .useForever
}
