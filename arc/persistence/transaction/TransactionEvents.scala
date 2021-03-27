package newages.casino.wallet.persistence.transaction

import newages.casino.wallet.model.Payment

object TransactionEvents {

  sealed trait TransactionEvent

  case class MakePayments(payments: Set[Payment]) extends TransactionEvent

  case object TransactionFailed extends TransactionEvent

  case object TransactionCompleted extends TransactionEvent

  val eventHandler: (
      TransactionStates.TransactionState,
      TransactionEvent
  ) => TransactionStates.TransactionState = {
    (state, event) => state.applyEvent(event)
  }
}
