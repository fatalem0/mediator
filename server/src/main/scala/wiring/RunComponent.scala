package wiring

import cats.effect.{ Async, Deferred, Resource }
import cats.syntax.functor._
import cats.syntax.monadError._
import org.http4s.server.Server
import utils.hooks.{ ShutdownHook, StartupHook }
import utils.server.{ ApiBuilder, Http4sServer, HttpMakeRoute, WireHttpModuleSyntaxOps }

class RunComponent[I[_]](
    val api: Resource[I, Server],
    val hook: StartupHook[I]
) {
  def resource: Resource[I, StartupHook[I]] = api.map(_ => hook)
}

object RunComponent {
  def make[I[_]: Async](
      core: CoreComponent[I],
      publicControllers: PublicControllers[I]
  )(implicit makeRoute: HttpMakeRoute.Http4s[I]): I[RunComponent[I]] = {
    import core._

    val shutdownHook = ShutdownHook.make[I](
      conf.shutdown.gracePeriod,
      probeControl
    )

    implicit val apiBuilder: ApiBuilder[I] = ApiBuilder.make[I]

    Deferred[I, Either[Throwable, Unit]].map { stopSignal =>
      val startupHook: StartupHook[I] = StartupHook.makeObservable[I](
        stopSignal.get.rethrow,
        probeControl,
        shutdownHook
      )

      new RunComponent[I](
        Http4sServer.buildEmber[I](
          makeRoute.makeRoute(
            publicControllers.wire
          ),
          conf.server
        ),
        startupHook
      )
    }
  }
}
