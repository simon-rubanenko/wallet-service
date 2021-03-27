package newages.casino.wallet.persistence.transaction

import akka.Done
import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import newages.casino.wallet.model.{Payment, TransactionId}
import newages.casino.wallet.persistence.ProtobufSerializable

object TransactionCommands {

  sealed trait TransactionCommand extends ProtobufSerializable

  final case class MakePayments(payments: Set[Payment], replyTo: ActorRef[StatusReply[Done]])
      extends TransactionCommand

  final case class TransactionFailed(replyTo: ActorRef[StatusReply[Done]])
      extends TransactionCommand

  final case class TransactionCompleted(replyTo: ActorRef[StatusReply[Done]])
      extends TransactionCommand

  def commandHandler(transactionId: TransactionId): (
      TransactionStates.TransactionState,
      TransactionCommand
  ) => ReplyEffect[TransactionEvents.TransactionEvent, TransactionStates.TransactionState] = {
    (state, cmd) =>
      state match {
        case TransactionStates.TransactionOpened =>
          cmd match {
            case c: MakePayments => makePayments(c)
            case _               => Effect.unhandled.thenNoReply()
          }

        case state @ TransactionStates.MakePayments(_) =>
          cmd match {
            case c: TransactionFailed    => transactionFailure(c)
            case c: TransactionCompleted => transactionComplete(c)
            case c: MakePayments => Effect.reply(c.replyTo)(
                StatusReply.Error(s"Transaction $transactionId is already opened")
              )
          }

        case TransactionStates.TransactionFailed => Effect.unhandled.thenNoReply()

        case TransactionStates.TransactionCompleted => Effect.unhandled.thenNoReply()
      }
  }

  private def makePayments(cmd: MakePayments)
      : ReplyEffect[TransactionEvents.TransactionEvent, TransactionStates.TransactionState] =
    Effect.persist(TransactionEvents.MakePayments(cmd.payments)).thenReply(cmd.replyTo)(_ =>
      StatusReply.Ack
    )

  private def transactionFailure(cmd: TransactionFailed)
      : ReplyEffect[TransactionEvents.TransactionEvent, TransactionStates.TransactionState] =
    Effect.persist(TransactionEvents.TransactionFailed).thenReply(cmd.replyTo)(_ => StatusReply.Ack)

  private def transactionComplete(cmd: TransactionCompleted)
      : ReplyEffect[TransactionEvents.TransactionEvent, TransactionStates.TransactionState] =
    Effect.persist(TransactionEvents.TransactionCompleted).thenReply(cmd.replyTo)(_ =>
      StatusReply.Ack
    )

}
