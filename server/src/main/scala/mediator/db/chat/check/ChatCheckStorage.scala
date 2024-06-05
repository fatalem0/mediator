package mediator.db.chat.check

import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Functor }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.chat.Domain.ChatCreate
import mediator.db.chat.ChatStorage
import mediator.db.chat.check.Domain.Errors.CheckError
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.feither._
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait ChatCheckStorage[F[_]] {
  def checkIfExists(req: ChatCreate.Request): F[Either[CheckError, Boolean]]
}

object ChatCheckStorage extends Logging.Companion[ChatCheckStorage] {
  final private class Impl[F[_]: Functor](storage: ChatStorage[F])
    extends ChatCheckStorage[F] {
    override def checkIfExists(req: ChatCreate.Request): F[Either[CheckError, Boolean]] =
      storage.get(req.initiatorId, req.friendId).map(_.isDefined).rightIn[CheckError]
  }

  final private class LogMid[F[_]: FlatMap: ChatCheckStorage.Log]
    extends ChatCheckStorage[Mid[F, *]] {
    override def checkIfExists(req: ChatCreate.Request): Mid[F, Either[CheckError, Boolean]] =
      debug"Checking if chat with initiator = ${req.initiatorId} and friend = ${req.friendId} exists" *>
        _.flatTap {
          case Left(CheckError.PSQL(cause)) =>
            errorCause"""
                        Failed to check is chat with initiator = ${req.initiatorId}
                        and friend = ${req.friendId} exists
                     """ (cause)
          case Left(CheckError.Connection(cause)) =>
            errorCause"""
                        Failed to check is chat with initiator = ${req.initiatorId}
                        and friend = ${req.friendId} exists due to connection error
                      """ (cause)
          case Right(true) =>
            debug"Successfully find chat with initiator = ${req.initiatorId} and friend = ${req.friendId}"
          case Right(false) =>
            debug"Cannot find chat with initiator = ${req.initiatorId} and friend = ${req.friendId}"
        }
  }

  private object Errors extends ChatCheckStorage[SQLErrorJoiner] {
    override def checkIfExists(req: ChatCreate.Request): SQLErrorJoiner[Either[
      CheckError,
      Boolean
    ]] =
      SQLErrorJoiner[Either[CheckError, Boolean]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: ChatCheckStorage[F] =
    DatabaseRunner[ChatCheckStorage, F].wire(
      new Impl[ConnectionIO](
        ChatStorage.db
      ),
      Errors
    )

  def makeObservable[F[_]: MonadCancelThrow: SafeTransactor: Logging.Make]: ChatCheckStorage[F] = {
    val logMid = new LogMid[F]: ChatCheckStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
