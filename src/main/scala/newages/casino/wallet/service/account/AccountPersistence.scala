package newages.casino.wallet.service.account

import cats.effect.IO
import doobie.implicits.toSqlInterpolator
import newages.casino.wallet.model.{AccountId, Amount}
import doobie.implicits._
import newages.casino.wallet.service.DoobiePersistence

trait AccountPersistence {
  def addAccount(accountId: AccountId): IO[Unit]
  def deposit(accountId: AccountId, amount: Amount): IO[Amount]
  def withdraw(accountId: AccountId, amount: Amount): IO[Amount]
  def getBalance(accountId: AccountId): IO[Amount]
}

object AccountPersistence {
  def apply(db: DoobiePersistence): AccountPersistence = new AccountPersistenceImpl(db)
}

class AccountPersistenceImpl(db: DoobiePersistence) extends AccountPersistence {
  override def addAccount(accountId: AccountId): IO[Unit] =
    sql"""insert into account.account(account_id, account_balance) values(${accountId.id}, 0)"""
      .update
      .run
      .transact(db.autoCommitTransactor)
      .map(_ => ())

  override def deposit(accountId: AccountId, amount: Amount): IO[Amount] =
    (for {
      balance <- fetchBalance(accountId)
      newBalance = balance + amount
      _ <- updateBalance(accountId, newBalance)
    } yield newBalance)
      .transact(db.defaultTransactor)

  override def withdraw(accountId: AccountId, amount: Amount): IO[Amount] =
    (for {
      balance <- fetchBalance(accountId)
      newBalance = balance - amount
      _ <- updateBalance(accountId, newBalance)
    } yield newBalance)
      .transact(db.defaultTransactor)

  override def getBalance(accountId: AccountId): IO[Amount] =
    fetchBalance(accountId)
      .transact(db.autoCommitTransactor)

  private def fetchBalance(accountId: AccountId): doobie.ConnectionIO[Amount] =
    sql"""select 
            account_balance 
          from account.account
          where account_id = ${accountId.id}"""
      .query[BigDecimal]
      .unique
      .map(Amount)

  private def updateBalance(accountId: AccountId, amount: Amount): doobie.ConnectionIO[Unit] =
    sql"""update account.account
            set account_balance = ${amount.value}
          where account_id = ${accountId.id}"""
      .update
      .run
      .map(_ => ())
}
