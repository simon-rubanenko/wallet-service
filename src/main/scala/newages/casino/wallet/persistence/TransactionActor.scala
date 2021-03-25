package newages.casino.wallet.persistence

import newages.casino.wallet.model.TransactionId

object TransactionActor {
  final case class TransactionState(transactionId: TransactionId) extends ProtobufSerializable
}
