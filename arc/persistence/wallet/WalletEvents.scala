package newages.casino.wallet.persistence.wallet

import newages.casino.wallet.model.{AccountId, CurrencyId}

object WalletEvents {

  sealed trait WalletEvent

  case object WalletCreated extends WalletEvent

  case class AccountAdded(accountId: AccountId, currencyId: CurrencyId) extends WalletEvent

  case object WalletClosed extends WalletEvent

  val eventHandler: (WalletStates.WalletState, WalletEvent) => WalletStates.WalletState = {
    (state, event) => state.applyEvent(event)
  }
}
