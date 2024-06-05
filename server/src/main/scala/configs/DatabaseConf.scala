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
  def makeDoobieConfig: Config = Config(
    jdbcUrl = Some(hikariPool.jdbcUrl),
    driverClassName = driverClassName,
    password = Some(hikariPool.password),
    username = Some(hikariPool.username),
    maximumPoolSize = Some(hikariPool.maximumPoolSize),
    maxLifetime = hikariPool.maxLifetime,
    minimumIdle = Some(hikariPool.minimumIdle),
    idleTimeout = hikariPool.idleTimeout,
    validationTimeout = hikariPool.validationTimeout,
    connectionTimeout = hikariPool.connectionTimeout,
    initializationFailTimeout = hikariPool.initializationFailTimeout,
    leakDetectionThreshold = hikariPool.leakDetectionThreshold,
    poolName = hikariPool.poolName
  )
}
