package wiring

import cats.effect.{ Async, Resource }
import doobie.Transactor
import users.db.users.UserStorage
import utils.db.{ DatabaseTransactor, SafeTransactor }

class DatabaseComponent[F[_]](implicit
    val transactor: Transactor[F],
    val safeTransactor: SafeTransactor[F],
    val userStorage: UserStorage[F]
)

object DatabaseComponent {
  def make[I[_]: Async](
      core: CoreComponent[I]
  ): Resource[I, DatabaseComponent[I]] = {
    import core._

    for {
      implicit0(transactor: Transactor[I]) <- DatabaseTransactor.make[I](
        conf.database
      )
      implicit0(safeTransactor: SafeTransactor[I]) = SafeTransactor.make[I](
        transactor
      )
      implicit0(userReadStorage: UserStorage[I]) = UserStorage.makeObservable[I]

      comp = new DatabaseComponent[I]
    } yield comp
  }
}
