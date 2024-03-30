package configs

import derevo.derive
import derevo.pureconfig.pureconfigReader
import doobie.hikari.Config

@derive(pureconfigReader)
final case class DatabaseConf(
    driverClassName: Option[String],
    awaitingThreads: Int,
    hikariPool: HikariPoolConf
) {
  def makeDoobieConfig: Config =
    Config(
      jdbcUrl = hikariPool.jdbcUrl,
      driverClassName = driverClassName,
      password = Some(hikariPool.password),
      username = Some(hikariPool.username),
      maximumPoolSize = hikariPool.maximumPoolSize,
      maxLifetime = hikariPool.maxLifetime,
      minimumIdle = hikariPool.minimumIdle,
      idleTimeout = hikariPool.idleTimeout,
      validationTimeout = hikariPool.validationTimeout,
      connectionTimeout = hikariPool.connectionTimeout,
      initializationFailTimeout = hikariPool.initializationFailTimeout,
      leakDetectionThreshold = hikariPool.leakDetectionThreshold,
      poolName = hikariPool.poolName
    )
}
