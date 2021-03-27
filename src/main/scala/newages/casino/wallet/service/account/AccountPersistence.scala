package newages.casino.wallet.service.account

import cats.effect.IO
import newages.casino.wallet.domain.ActionResult
import newages.casino.wallet.model.{AccountId, Amount}

trait AccountPersistence {
  def deposit(accountId: AccountId, amount: Amount): IO[ActionResult[Amount]]
  def withdraw(accountId: AccountId, amount: Amount): IO[ActionResult[Amount]]
}

object AccountPersistence {
  def apply(): AccountPersistence = new AccountPersistenceImpl()
}

class AccountPersistenceImpl extends AccountPersistence {
  override def deposit(accountId: AccountId, amount: Amount): IO[ActionResult[Amount]] = ???

  override def withdraw(accountId: AccountId, amount: Amount): IO[ActionResult[Amount]] = ???
}
