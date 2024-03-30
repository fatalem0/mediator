package utils

import cats.data.EitherT
import doobie.Transactor
import utils.db.model.DatabaseError

package object db {
  type SafeTransactor[F[_]] = Transactor[EitherT[F, DatabaseError, *]]
}
