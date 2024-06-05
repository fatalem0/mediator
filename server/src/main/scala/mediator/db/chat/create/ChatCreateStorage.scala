package mediator.db.chat.create

import cats.effect.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{ FlatMap, Functor }
import derevo.derive
import derevo.tagless.applyK
import doobie.ConnectionIO
import mediator.db.chat.ChatStorage
import mediator.db.chat.create.Domain.Errors.CreateError
import mediator.db.chat.get.Domain.ChatRow
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging._
import utils.db.{ DatabaseRunner, SafeTransactor, SQLErrorJoiner }

@derive(applyK)
trait ChatCreateStorage[F[_]] {
  def create(chatRow: ChatRow): F[Either[CreateError, Unit]]
}

object ChatCreateStorage extends Logging.Companion[ChatCreateStorage] {
  final private class Impl[F[_]: Functor](storage: ChatStorage[F])
    extends ChatCreateStorage[F] {
    override def create(chatRow: ChatRow): F[Either[CreateError, Unit]] =
      storage.create(chatRow).map(Either.cond(_, (), CreateError.NoUpdate))
  }

  final private class LogMid[F[_]: FlatMap: ChatCreateStorage.Log]
    extends ChatCreateStorage[Mid[F, *]] {
    override def create(chatRow: ChatRow): Mid[F, Either[CreateError, Unit]] =
      debug"Trying to create chat for initiator = ${chatRow.initiatorId} and friend = ${chatRow.friendId}" *>
        _.flatTap {
          case Left(CreateError.NoUpdate) => warn"""
                  Chat for initiator = ${chatRow.initiatorId} and
                  friend = ${chatRow.friendId} hasn't been created
                """
          case Left(CreateError.AlreadyExists(_)) =>
            warn"Chat for initiator = ${chatRow.initiatorId} and friend = ${chatRow.friendId} already exists"
          case Left(CreateError.PSQL(cause)) =>
            errorCause"""
                        Failed to create chat for initiator = ${chatRow.initiatorId}
                        and friend = ${chatRow.friendId}
                      """ (cause)
          case Left(CreateError.Connection(cause)) =>
            errorCause"""
                        Failed to create chat with for initiator = ${chatRow.initiatorId}
                        and friend = ${chatRow.friendId} due to connection error
                      """ (cause)
          case Right(_) =>
            debug"""
                   Successfully created chat with for initiator = ${chatRow.initiatorId}
                   and friend = ${chatRow.friendId}
                 """
        }
  }

  private object Errors extends ChatCreateStorage[SQLErrorJoiner] {
    override def create(chatRow: ChatRow): SQLErrorJoiner[Either[CreateError, Unit]] =
      SQLErrorJoiner[Either[CreateError, Unit]]
  }

  def make[F[_]: MonadCancelThrow: SafeTransactor]: ChatCreateStorage[F] =
    DatabaseRunner[ChatCreateStorage, F].wire(
      new Impl[ConnectionIO](
        ChatStorage.db
      ),
      Errors
    )

  def makeObservable[F[_]: MonadCancelThrow: SafeTransactor: Logging.Make]: ChatCreateStorage[F] = {
    val logMid = new LogMid[F]: ChatCreateStorage[Mid[F, *]]

    logMid attach make[F]
  }
}
