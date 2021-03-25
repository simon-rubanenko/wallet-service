package newages.casino.wallet.persistence.wallet

import newages.casino.wallet.model.{AccountId, CurrencyId}

object WalletStates {

  import WalletEvents._

  sealed trait WalletState {
    def applyEvent(event: WalletEvents.WalletEvent): WalletState
  }

  case object InitialWallet extends WalletState {
    def applyEvent(event: WalletEvent): WalletState = event match {
      case WalletEvents.WalletCreated => WalletCreated(Map.empty)
      case _ =>
        throw new IllegalStateException(s"Unexpected event [$event] in state [InitialWallet]")
    }
  }

  case class WalletCreated(accounts: Map[AccountId, AccountInfo]) extends WalletState {
    def applyEvent(event: WalletEvents.WalletEvent): WalletState = event match {
      case WalletEvents.AccountCreated(accountId, currencyId) =>
        copy(accounts = accounts + (accountId -> AccountInfo(currencyId)))

      case WalletEvents.AccountClosed(accountId) => copy(accounts - accountId)

      case WalletEvents.WalletClosed => WalletClosed

      case WalletEvents.WalletCreated =>
        throw new IllegalStateException(s"Unexpected event [$event] in state [WalletCreated]")
    }
  }

  case object WalletClosed extends WalletState {
    def applyEvent(event: WalletEvents.WalletEvent): WalletState =
      throw new IllegalStateException(s"Unexpected event [$event] in state [WalletClosed]")
  }

  final case class AccountInfo(currencyId: CurrencyId)
}
