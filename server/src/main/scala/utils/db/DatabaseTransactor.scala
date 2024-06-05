package utils.db

import cats.effect.Async
import cats.effect.kernel.Resource
import configs.DatabaseConf
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object DatabaseTransactor {
  def make[I[_]: Async](config: DatabaseConf): Resource[I, Transactor[I]] =
    for {
      connectEC <- ExecutionContexts.fixedThreadPool[I](config.awaitingThreads)
      doobieConfig = config.makeDoobieConfig

      initialTransactor <- HikariTransactor.fromConfig[I](
        doobieConfig,
        connectEC
      )
    } yield initialTransactor
}
