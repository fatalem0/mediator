package utils

import sttp.tapir.server.ServerEndpoint

package object server {
  type HttpModule[F[_]] = List[ServerEndpoint[Any, F]]

  implicit final class WireHttpModuleSyntaxOps[Controller[f[_]], F[_]](
      private val controller: Controller[F]
  ) extends AnyVal {
    def wire(implicit
        wiring: WireWithLogic[Controller],
        apiBuilder: ApiBuilder[F]
    ): HttpModule[F] = wiring.wire[F](controller)
  }
}
