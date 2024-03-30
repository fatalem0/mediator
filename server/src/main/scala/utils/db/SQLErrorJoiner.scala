package utils.db

import utils.db.model.DatabaseError

trait SQLErrorJoiner[+Result] {
  def join(error: DatabaseError): Result
}

object SQLErrorJoiner {
  def apply[Result](implicit
      joiner: SQLErrorJoiner[Result]
  ): SQLErrorJoiner[Result] = joiner

  implicit def deriveForEither[E, A](implicit
      joiner: SQLErrorJoiner[E]
  ): SQLErrorJoiner[Either[E, A]] =
    error => Left(joiner.join(error))
}
