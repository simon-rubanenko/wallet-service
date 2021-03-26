package newages.casino.wallet.persistence.transaction

import newages.casino.wallet.model.{Currency, Payment}
import newages.casino.wallet.persistence.transaction.TransactionEvents.TransactionEvent

object TransactionStates {

  sealed trait TransactionState {
    def applyEvent(event: TransactionEvents.TransactionEvent): TransactionState
  }

  case object TransactionOpened extends TransactionState {
    def applyEvent(event: TransactionEvent): TransactionState = event match {
      case TransactionEvents.MakePayments(payments) => MakePayments(payments)
      case _ =>
        throw new IllegalStateException(s"Unexpected event [$event] in state [TransactionOpened]")
    }
  }

  case class MakePayments(payments: Set[Payment]) extends TransactionState {
    def applyEvent(event: TransactionEvents.TransactionEvent): TransactionState = event match {
      case TransactionEvents.TransactionFailed => TransactionFailed

      case TransactionEvents.TransactionCompleted => TransactionCompleted

      case _: TransactionEvents.MakePayments =>
        throw new IllegalStateException(s"Unexpected event [$event] in state [MakePayments]")
    }
  }

  case object TransactionFailed extends TransactionState {
    def applyEvent(event: TransactionEvents.TransactionEvent): TransactionState =
      throw new IllegalStateException(s"Unexpected event [$event] in state [TransactionFailed]")
  }

  case object TransactionCompleted extends TransactionState {
    def applyEvent(event: TransactionEvents.TransactionEvent): TransactionState =
      throw new IllegalStateException(s"Unexpected event [$event] in state [TransactionCompleted]")
  }

  final case class AccountInfo(currencyId: Currency)

}
