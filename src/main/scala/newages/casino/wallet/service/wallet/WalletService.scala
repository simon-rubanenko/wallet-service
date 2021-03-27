package newages.casino.wallet.service.wallet

import cats.effect.IO
import newages.casino.wallet.domain.ActionResult
import newages.casino.wallet.model.{AccountId, Currency, CurrencyId, WalletId}
import newages.casino.wallet.service.account.AccountService

trait WalletService {
  def createWallet: IO[ActionResult[WalletId]]

  def createAccount(currency: Currency): IO[ActionResult[AccountId]]

  def findAccountId(currencyId: CurrencyId): IO[Option[AccountId]]

}

object WalletService {
  def apply(accountService: AccountService, persistence: WalletPersistence): WalletService =
    new WalletServiceImpl(accountService, persistence)
}

class WalletServiceImpl(
    accountService: AccountService,
    persistence: WalletPersistence
) extends WalletService {
  override def createWallet: IO[ActionResult[WalletId]] = ???

  override def createAccount(currency: Currency): IO[ActionResult[AccountId]] = ???

  override def findAccountId(currencyId: CurrencyId): IO[Option[AccountId]] = ???
}
