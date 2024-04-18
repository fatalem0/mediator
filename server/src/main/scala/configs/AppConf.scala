package configs

import cats.effect.Sync
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource

@derive(pureconfigReader)
final case class AppConf(
    server: ServerConf,
    shutdown: ShutdownConf,
    auth: AuthConf,
    database: DatabaseConf,
    integration: IntegrationConf
)

object AppConf {
  def load[I[_]: Sync]: I[AppConf] =
    Sync[I].delay(ConfigSource.default.loadOrThrow[AppConf])
}
