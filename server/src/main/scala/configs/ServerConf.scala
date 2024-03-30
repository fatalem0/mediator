package configs

import com.comcast.ip4s.{ Host, Port }
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

import scala.concurrent.duration.FiniteDuration

@derive(pureconfigReader)
case class ServerConf(
    host: Host,
    port: Port,
    shutdownTimeout: FiniteDuration
)

object ServerConf {
  implicit val portConfigReader: ConfigReader[Port] =
    ConfigReader[Int].emap(i =>
      Port
        .fromInt(i)
        .toRight(CannotConvert(i.toString, "Port", "is not a port"))
    )

  implicit val hostConfigReader: ConfigReader[Host] =
    ConfigReader[String].emap(s =>
      Host.fromString(s).toRight(CannotConvert(s, "Host", "is not a host"))
    )
}
