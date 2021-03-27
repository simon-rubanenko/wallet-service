package newages.casino.wallet.persistence.wallet

import akka.Done
import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import newages.casino.wallet.model.{AccountId, ActionResult, CurrencyId, WalletId}
import newages.casino.wallet.persistence.ProtobufSerializable
import newages.casino.wallet.persistence.wallet.WalletStates.AccountInfo

object WalletCommands {

  final case class WalletDetails(accounts: Map[AccountId, AccountInfo])

  sealed trait WalletCommand extends ProtobufSerializable

  final case class CreateWallet(replyTo: ActorRef[ActionResult[Done]]) extends WalletCommand

  final case class CloseWallet(replyTo: ActorRef[StatusReply[Done]]) extends WalletCommand

  final case class AddAccount(
      accountId: AccountId,
      currencyId: CurrencyId,
      replyTo: ActorRef[ActionResult[WalletDetails]]
  ) extends WalletCommand

  def commandHandler(walletId: WalletId): (
      WalletStates.WalletState,
      WalletCommand
  ) => ReplyEffect[WalletEvents.WalletEvent, WalletStates.WalletState] = {
    (state, cmd) =>
      state match {
        case WalletStates.InitialWallet =>
          cmd match {
            case c: CreateWallet => createWallet(c)
            case _               => Effect.unhandled.thenNoReply()
          }

        case state @ WalletStates.WalletCreated(_) =>
          cmd match {
            case c: AddAccount  => addAccount(c)
            case c: CloseWallet => closeWallet(state, c)
            case c: CreateWallet => Effect.reply(c.replyTo)(
                ActionResult.error(s"Wallet $walletId is already created")
              )
          }

        case WalletStates.WalletClosed => Effect.unhandled.thenNoReply()
      }
  }

  private def createWallet(cmd: CreateWallet)
      : ReplyEffect[WalletEvents.WalletEvent, WalletStates.WalletState] =
    Effect.persist(WalletEvents.WalletCreated).thenReply(cmd.replyTo)(_ => ActionResult.done)

  private def addAccount(cmd: AddAccount)
      : ReplyEffect[WalletEvents.WalletEvent, WalletStates.WalletState] =
    Effect.persist(WalletEvents.AccountAdded(cmd.accountId, cmd.currencyId)).thenReply(
      cmd.replyTo
    )(state => ActionResult.success(WalletDetails(state.getAccounts)))

  private def closeWallet(
      state: WalletStates.WalletCreated,
      cmd: CloseWallet
  ): ReplyEffect[WalletEvents.WalletEvent, WalletStates.WalletState] =
    if (state.canCloseWallet)
      Effect.persist(WalletEvents.WalletClosed).thenReply(cmd.replyTo)(_ => StatusReply.Ack)
    else
      Effect.reply(cmd.replyTo)(replyAllAccountMustBeClosed)

  private[wallet] def replyAllAccountMustBeClosed[T]: StatusReply[T] =
    StatusReply.Error(s"All accounts in wallet must be closed")
}
