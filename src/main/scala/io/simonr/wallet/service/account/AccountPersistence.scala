package io.simonr.wallet.service.account

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Props, Scheduler, SpawnProtocol}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import cats.effect.{ContextShift, IO}
import cats.implicits._
import doobie.implicits.toSqlInterpolator
import doobie.implicits._
import io.simonr.utils.doobie.DoobiePersistence
import io.simonr.wallet.model.{AccountId, Amount}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

trait AccountPersistence {
  def addAccount(accountId: AccountId): IO[Unit]
  def deposit(accountId: AccountId, amount: Amount): IO[Amount]
  def withdraw(accountId: AccountId, amount: Amount): IO[Amount]
  def getBalance(accountId: AccountId): IO[Amount]
}

object AccountPersistence {
  def apply(db: DoobiePersistence)(implicit cs: ContextShift[IO]): AccountPersistence =
    new AccountPersistenceImpl(db)
}

class AccountPersistenceImpl(db: DoobiePersistence)(implicit cs: ContextShift[IO])
    extends AccountPersistence {
  override def addAccount(accountId: AccountId): IO[Unit] =
    sql"""insert into wallet_account.account(account_id, account_balance) values(${accountId.id}, 0)"""
      .update
      .run
      .transact(db.autoCommitTransactor)
      .map(_ => ())

  override def deposit(accountId: AccountId, amount: Amount): IO[Amount] = {
    def trx = (for {
      balance <- fetchBalance(accountId)
      newBalance = balance + amount
      _ <- updateBalance(accountId, newBalance)
    } yield newBalance)
      .transact(db.defaultTransactor)

    for {
      actor <- getActorForAccount(accountId)
      amount <- IO.fromFuture(IO(actor.ask(AccountActor.Deposit(() => trx, _)))).flatten
    } yield amount
  }

  override def withdraw(accountId: AccountId, amount: Amount): IO[Amount] = {
    def trx =
      for {
        balance <- fetchBalance(accountId).transact(db.defaultTransactor)
        _ <-
          if (balance.value < amount.value) IO.raiseError(new Throwable("Insufficient funds"))
          else IO.unit
        newBalance = balance - amount
        _ <- updateBalance(accountId, newBalance).transact(db.defaultTransactor)
      } yield newBalance

    for {
      actor <- getActorForAccount(accountId)
      amount <- IO.fromFuture(IO(actor.ask(AccountActor.Withdraw(() => trx, _)))).flatten
    } yield amount

  }

  override def getBalance(accountId: AccountId): IO[Amount] =
    fetchBalance(accountId)
      .transact(db.autoCommitTransactor)

  private def fetchBalance(accountId: AccountId): doobie.ConnectionIO[Amount] =
    sql"""select 
            account_balance 
          from wallet_account.account
          where account_id = ${accountId.id}"""
      .query[BigDecimal]
      .unique
      .map(Amount)

  private def updateBalance(accountId: AccountId, amount: Amount): doobie.ConnectionIO[Unit] =
    sql"""update wallet_account.account
            set account_balance = ${amount.value}
          where account_id = ${accountId.id}"""
      .update
      .run
      .map(_ => ())

  implicit val system: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "root")
  implicit val ecActor: ExecutionContext = system.executionContext
  implicit lazy val timeout: Timeout = 5.seconds
  implicit val scheduler: Scheduler = system.scheduler

  private def getActorForAccount(accountId: AccountId): IO[ActorRef[AccountActor.Protocol]] = {
    val future: Future[ActorRef[AccountActor.Protocol]] = system.ask(SpawnProtocol.Spawn(
      behavior = AccountActor(),
      name = s"account-${accountId.id}",
      props = Props.empty,
      _
    ))
    IO.fromFuture[ActorRef[AccountActor.Protocol]](IO(future))
  }
}

private object AccountActor {

  sealed trait Protocol
  final case class Withdraw(f: () => IO[Amount], replyTo: ActorRef[IO[Amount]]) extends Protocol
  final case class Deposit(f: () => IO[Amount], replyTo: ActorRef[IO[Amount]]) extends Protocol

  def apply(): Behavior[Protocol] =
    Behaviors.setup[Protocol] { _ =>
      Behaviors.receive[Protocol] { (ctx, message) =>
        message match {
          case Withdraw(f, replyTo) =>
            ctx.log.debug("Withdraw transaction")
            replyTo ! f()
            Behaviors.same

          case Deposit(f, replyTo) =>
            ctx.log.debug("Deposit transaction")
            replyTo ! f()
            Behaviors.same
        }
      }
    }
}
