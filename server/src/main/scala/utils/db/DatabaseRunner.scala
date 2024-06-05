package utils.db

import cats.effect.MonadCancelThrow
import cats.tagless.ApplyK
import doobie.ConnectionIO
import doobie.syntax.connectionio._
import tofu.syntax.funk._

trait DatabaseRunner[Module[_[_]], Expr[_]] {
  def wire(
      connections: Module[ConnectionIO],
      errors: Module[SQLErrorJoiner]
  ): Module[Expr]
}

object DatabaseRunner {
  def apply[Module[_[_]], Expr[_]](implicit
      runner: DatabaseRunner[Module, Expr]
  ): DatabaseRunner[Module, Expr] = runner

  final private class Impl[Module[_[_]]: ApplyK, Expr[_]: MonadCancelThrow](
      transactor: SafeTransactor[Expr]
  ) extends DatabaseRunner[Module, Expr] {
    override def wire(
        connections: Module[ConnectionIO],
        errors: Module[SQLErrorJoiner]
    ): Module[Expr] =
      ApplyK[Module].map2K(
        connections,
        errors
      )(funK(r => r.first.transact(transactor).fold(r.second.join, identity)))
  }

  implicit def derive[Module[_[_]]: ApplyK, Expr[_]: MonadCancelThrow](implicit
      transactor: SafeTransactor[Expr]
  ): DatabaseRunner[Module, Expr] = new Impl(transactor)

  def make[Module[_[_]]: ApplyK, Expr[_]: MonadCancelThrow](
      transactor: SafeTransactor[Expr]
  ): DatabaseRunner[Module, Expr] = new Impl(transactor)
}
