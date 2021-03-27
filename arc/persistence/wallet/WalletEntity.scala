package newages.casino.wallet.persistence.wallet

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, RetentionCriteria}
import newages.casino.wallet.model.WalletId

import scala.concurrent.duration.DurationInt

object WalletEntity {
  def apply(walletId: WalletId): Behavior[WalletCommands.WalletCommand] =
    EventSourcedBehavior[
      WalletCommands.WalletCommand,
      WalletEvents.WalletEvent,
      WalletStates.WalletState
    ](
      PersistenceId("Wallet", walletId.id),
      WalletStates.InitialWallet,
      WalletCommands.commandHandler(walletId),
      WalletEvents.eventHandler
    )
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
}
