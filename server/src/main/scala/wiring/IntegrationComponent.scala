package wiring

import cats.effect.{Async, Resource}
import integrations.discogs.DiscogsTokenClient
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import tethys.jackson._

class IntegrationComponent[F[_]](implicit
    val discogsTokenClient: DiscogsTokenClient[F]
)

object IntegrationComponent {
  def make[F[_]: Async](core: CoreComponent[F]): Resource[F, IntegrationComponent[F]] = {
    import core._

    for {
      backend: SttpBackend[F, Any] <- buildAsyncBackend[F]

      implicit0(discogsTokenClient: DiscogsTokenClient[F]) =
        DiscogsTokenClient.make[F](backend, core.conf.integration.discogs)

      comp = new IntegrationComponent[F]()
    } yield comp
  }

  private def buildAsyncBackend[F[_]: Async]: Resource[F, SttpBackend[F, Any]] =
    Resource.make(AsyncHttpClientCatsBackend[F]())(_.close())
}
