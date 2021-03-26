package newages.casino.wallet.cats

import cats.effect.IO
import newages.casino.wallet.cats.Transaction.OperationResult
import newages.casino.wallet.cats.domain.{ActionResult, Done}
import newages.casino.wallet.model.{AccountId, AccountInfo, Currency, CurrencyId, Money, WalletId}

object domain {
  case object Error extends Throwable

  type ActionResult[T] = Either[Error, T]

  sealed abstract class Done

  case object Done extends Done {
    def getInstance(): Done = this
    def done(): Done = this
  }

  object ActionResult {
    val done: Done = Done
  }
}

trait Wallet {
  def createWallet: IO[ActionResult[WalletId]]

  def createAccount(currency: Currency): IO[ActionResult[AccountId]]

  def findAccountId(currencyId: CurrencyId): IO[Option[AccountId]]

}

case class WalletDao()

trait Persistent {
  def createWallet(wallet: WalletDao): IO[ActionResult[Done]]
  def createAccount(account: AccountInfo): IO[ActionResult[Done]]

}

trait Account {
  def createAccount: IO[ActionResult[AccountId]]
}

trait Transaction {
  def make(
      accountFrom: AccountId,
      accountTo: AccountId,
      amount: Money
  ): IO[ActionResult[OperationResult]]

}

object Transaction {
  case class OperationResult(accountFromInfo: AccountInfo, accountToInfo: AccountInfo)
}
