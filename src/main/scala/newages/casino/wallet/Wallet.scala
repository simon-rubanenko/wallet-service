package newages.casino.wallet

import cats.effect.IO
import newages.casino.wallet.Transaction.OperationResult
import newages.casino.wallet.domain.{ActionResult, Done}
import newages.casino.wallet.model._

object domain {
  case object Error extends Throwable

  type ActionResult[T] = Either[Error, T]

  sealed abstract class Done

  case object Done extends Done {
    def getInstance(): Done = this
    def done(): Done = this
  }

  object ActionResult {
    val done = Right(Done)
    def success[A](a: A): ActionResult[A] = Right(a)
  }
}

case class WalletDao()

trait TransactionService {
  def make(
      accountFrom: AccountId,
      accountTo: AccountId,
      amount: Money
  ): IO[ActionResult[OperationResult]]

}

object Transaction {
  case class OperationResult(accountFromInfo: AccountInfo, accountToInfo: AccountInfo)
}
