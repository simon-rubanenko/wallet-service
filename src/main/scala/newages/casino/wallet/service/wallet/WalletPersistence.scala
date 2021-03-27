package newages.casino.wallet.service.wallet

import cats.effect.IO
import newages.casino.wallet.domain.{ActionResult, Done}
import newages.casino.wallet.model.AccountId

trait WalletPersistence {
  def addAccount(accountId: AccountId): IO[ActionResult[Done]]
}

object WalletPersistence {
  def apply(): WalletPersistence = new WalletPersistenceImpl()
}

class WalletPersistenceImpl extends WalletPersistence {
  def addAccount(accountId: AccountId): IO[ActionResult[Done]] = ???

}
