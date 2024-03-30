import cats.effect.Async
import cats.effect.kernel.Resource
import sttp.tapir.server.http4s.Http4sServerOptions
import utils.hooks.StartupHook
import utils.server.HttpMakeRoute
import wiring._

object Application {
  implicit def httpMakeRoute[I[_]: Async]: HttpMakeRoute.Http4s[I] =
    HttpMakeRoute.make[I](Http4sServerOptions.default[I])

  def run[I[_]: Async]: Resource[I, StartupHook[I]] =
    for {
      core: CoreComponent[I]         <- Resource.eval(CoreComponent.make[I])
      database: DatabaseComponent[I] <- DatabaseComponent.make[I](core)
      services: ServiceComponent[I] = ServiceComponent.make[I](core, database)
      publicControllers: PublicControllers[I] = PublicControllers.make[I](
        services
      )

      run: RunComponent[I] <-
        Resource.eval(
          RunComponent.make[I](
            core,
            publicControllers
          )
        )

      hook: StartupHook[I] <- run.resource
    } yield hook
}
