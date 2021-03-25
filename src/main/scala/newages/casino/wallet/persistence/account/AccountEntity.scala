package newages.casino.wallet.persistence.account

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, RetentionCriteria}
import newages.casino.wallet.model.AccountId

import scala.concurrent.duration.DurationInt

object AccountEntity {
  def apply(accountId: AccountId): Behavior[AccountCommands.AccountCommand] =
    EventSourcedBehavior[
      AccountCommands.AccountCommand,
      AccountEvents.AccountEvent,
      AccountStates.AccountState
    ](
      PersistenceId("Account", accountId.id),
      AccountStates.InitialAccount,
      AccountCommands.commandHandler(accountId),
      AccountEvents.eventHandler
    )
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
}
