package newages.casino.wallet.persistence.account

import newages.casino.wallet.model.{AccountId, Money}

object AccountStates {
  sealed trait AccountState {
    def applyEvent(event: AccountEvents.AccountEvent): AccountState
    def getBalance: Money
  }

  case object InitialAccount extends AccountState {
    def applyEvent(event: AccountEvents.AccountEvent): AccountState = event match {
      case AccountEvents.AccountCreated => AccountOpened(Money.Zero)
      case _ =>
        throw new IllegalStateException(s"Unexpected event [$event] in state [InitialAccount]")
    }

    def getBalance: Money = Money.Zero
  }

  case class AccountOpened(balance: Money) extends AccountState {
    require(balance >= Money.Zero, "Account balance can't be negative")

    def applyEvent(event: AccountEvents.AccountEvent): AccountState = event match {
      case AccountEvents.Deposited(amount) => copy(balance = balance + amount)
      case AccountEvents.Withdrawn(amount) => copy(balance = balance - amount)
      case AccountEvents.AccountClosed     => AccountClosed
      case AccountEvents.AccountCreated =>
        throw new IllegalStateException(s"Unexpected event [$event] in state [AccountOpened]")
    }

    def getBalance: Money = balance

    def canWithdraw(amount: Money): Boolean =
      balance - amount >= Money.Zero
  }

  case object AccountClosed extends AccountState {
    def applyEvent(event: AccountEvents.AccountEvent): AccountState =
      throw new IllegalStateException(s"Unexpected event [$event] in state [AccountClosed]")

    def getBalance: Money = Money.Zero
  }
}
