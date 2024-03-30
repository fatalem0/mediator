package utils.db

import cats.data.EitherT
import cats.effect.MonadCancelThrow
import cats.syntax.applicativeError._
import cats.~>
import doobie.Transactor
import utils.db.model.DatabaseError
import utils.db.model.DatabaseError.fromThrowable

object SafeTransactor {
  private def funkF[F[_]: MonadCancelThrow]: F ~> EitherT[F, DatabaseError, *] =
    new (F ~> EitherT[F, DatabaseError, *]) {
      override def apply[A](fa: F[A]): EitherT[F, DatabaseError, A] =
        MonadCancelThrow[EitherT[F, DatabaseError, *]].uncancelable(_ =>
          EitherT(fa.attempt).leftMap(fromThrowable)
        )
    }

  def make[I[_]: MonadCancelThrow](
      transactor: Transactor[I]
  ): SafeTransactor[I] = transactor.mapK(funkF)
}
