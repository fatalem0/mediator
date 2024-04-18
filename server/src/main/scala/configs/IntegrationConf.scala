package configs

import derevo.derive
import derevo.pureconfig.pureconfigReader
import integrations.discogs.configs.DiscogsConfig

@derive(pureconfigReader)
final case class IntegrationConf(
    discogs: DiscogsConfig
  )

object IntegrationConf {

}