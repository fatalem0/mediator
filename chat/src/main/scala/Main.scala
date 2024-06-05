import Server.server
import cats.effect.std.Queue
import cats.effect.{ IO, IOApp }
import fs2.Stream
import fs2.concurrent.Topic
import org.http4s.websocket.WebSocketFrame

import scala.concurrent.duration._

object Main extends IOApp.Simple {
  def program: IO[Unit] = {
    for {
      q <- Queue.unbounded[IO, WebSocketFrame]
      t <- Topic[IO, WebSocketFrame]
      s <-
        Stream(
          Stream.fromQueueUnterminated(q).through(t.publish),
          Stream
            .awakeEvery[IO](30.seconds)
            .map(_ => WebSocketFrame.Ping())
            .through(t.publish),
          Stream.eval(server[IO](q, t))
        ).parJoinUnbounded.compile.drain
    } yield s
  }

  override def run: IO[Unit] = program
}
