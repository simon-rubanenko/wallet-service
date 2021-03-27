package newages.casino.wallet.persistence.account

import newages.casino.wallet.model.Money

object AccountEvents {

  sealed trait AccountEvent

  case object AccountCreated extends AccountEvent

  case class Deposited(amount: Money) extends AccountEvent

  case class Withdrawn(amount: Money) extends AccountEvent

  case object AccountClosed extends AccountEvent

  val eventHandler: (AccountStates.AccountState, AccountEvent) => AccountStates.AccountState = {
    (state, event) => state.applyEvent(event)
  }
}
