package configs

import derevo.derive
import derevo.pureconfig.pureconfigReader

import scala.concurrent.duration.FiniteDuration

@derive(pureconfigReader)
final case class ShutdownConf(
    gracePeriod: FiniteDuration
)
