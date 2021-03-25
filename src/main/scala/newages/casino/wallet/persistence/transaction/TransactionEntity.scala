package newages.casino.wallet.persistence.transaction

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, RetentionCriteria}
import newages.casino.wallet.model.TransactionId

import scala.concurrent.duration.DurationInt

object TransactionEntity {
  def apply(transactionId: TransactionId): Behavior[TransactionCommands.TransactionCommand] =
    EventSourcedBehavior[
      TransactionCommands.TransactionCommand,
      TransactionEvents.TransactionEvent,
      TransactionStates.TransactionState
    ](
      PersistenceId("Transaction", transactionId.id),
      TransactionStates.TransactionOpened,
      TransactionCommands.commandHandler(transactionId),
      TransactionEvents.eventHandler
    )
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
}
