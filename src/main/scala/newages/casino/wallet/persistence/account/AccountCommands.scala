package newages.casino.wallet.persistence.account

import akka.Done
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import newages.casino.wallet.model.{AccountId, ActionResult, Money}
import newages.casino.wallet.persistence.ProtobufSerializable

object AccountCommands {
  final case class AccountBalance(balance: Money) extends ProtobufSerializable

  sealed trait AccountCommand extends ProtobufSerializable

  final case class CreateAccount(replyTo: ActorRef[ActionResult[Done]]) extends AccountCommand

  final case class Deposit(
      amount: Money,
      replyTo: ActorRef[ActionResult[AccountBalance]]
  ) extends AccountCommand

  final case class Withdraw(
      amount: Money,
      replyTo: ActorRef[ActionResult[AccountBalance]]
  ) extends AccountCommand

  final case class GetBalance(walletId: AccountId, replyTo: ActorRef[AccountBalance])
      extends AccountCommand

  final case class CloseAccount(replyTo: ActorRef[ActionResult[Done]]) extends AccountCommand

  def commandHandler(accountId: AccountId): (
      AccountStates.AccountState,
      AccountCommand
  ) => ReplyEffect[AccountEvents.AccountEvent, AccountStates.AccountState] = {
    (state, cmd) =>
      state match {
        case AccountStates.InitialAccount =>
          cmd match {
            case c: CreateAccount => createAccount(c)
            case _                => Effect.unhandled.thenNoReply()
          }

        case state @ AccountStates.AccountOpened(_) =>
          cmd match {
            case c: Deposit      => deposit(c)
            case c: Withdraw     => withdraw(state, c)
            case c: GetBalance   => getBalance(state, c)
            case c: CloseAccount => closeAccount(state, c)
            case c: CreateAccount =>
              Effect.reply(c.replyTo)(
                ActionResult.error(s"Account $accountId is already created")
              )
          }

        case AccountStates.AccountClosed => Effect.unhandled.thenNoReply()
      }
  }

  private def createAccount(cmd: CreateAccount)
      : ReplyEffect[AccountEvents.AccountEvent, AccountStates.AccountState] =
    Effect.persist(AccountEvents.AccountCreated).thenReply(cmd.replyTo)(_ => ActionResult.done)

  private def deposit(cmd: Deposit)
      : ReplyEffect[AccountEvents.AccountEvent, AccountStates.AccountState] =
    Effect.persist(AccountEvents.Deposited(cmd.amount)).thenReply(cmd.replyTo)(state =>
      ActionResult.success(AccountBalance(state.getBalance))
    )

  private def withdraw(
      state: AccountStates.AccountOpened,
      cmd: Withdraw
  ): ReplyEffect[AccountEvents.AccountEvent, AccountStates.AccountState] =
    if (state.canWithdraw(cmd.amount))
      Effect.persist(AccountEvents.Withdrawn(cmd.amount)).thenReply(cmd.replyTo)(state =>
        ActionResult.success(AccountBalance(state.getBalance))
      )
    else
      Effect.reply(cmd.replyTo)(replyInsufficientBalance(state.balance, cmd.amount))

  private def getBalance(
      state: AccountStates.AccountOpened,
      cmd: GetBalance
  ): ReplyEffect[AccountEvents.AccountEvent, AccountStates.AccountState] =
    Effect.reply(cmd.replyTo)(AccountBalance(state.balance))

  private def closeAccount(
      state: AccountStates.AccountOpened,
      cmd: CloseAccount
  ): ReplyEffect[AccountEvents.AccountEvent, AccountStates.AccountState] =
    if (state.balance == Money.Zero)
      Effect.persist(AccountEvents.AccountClosed).thenReply(cmd.replyTo)(_ => ActionResult.done)
    else
      Effect.reply(cmd.replyTo)(replyCantCloseAccountWithNonZeroBalance)

  private[account] def replyInsufficientBalance[T](
      balance: Money,
      withdraw: Money
  ): ActionResult[T] =
    ActionResult.error(s"Insufficient balance $balance to be able to withdraw $withdraw")

  private[account] def replyCantCloseAccountWithNonZeroBalance[T]: ActionResult[T] =
    ActionResult.error("Can't close account with non-zero balance")
}
