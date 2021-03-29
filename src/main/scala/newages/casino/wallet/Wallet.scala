package newages.casino.wallet

import cats.effect.IO
import newages.casino.wallet.Transaction.OperationResult
import newages.casino.wallet.domain.ActionResult
import newages.casino.wallet.model._

object domain {
  case class Error(message: String) extends Throwable(message)

  type ActionResult[T] = Either[Error, T]

  sealed abstract class Done

  case object Done extends Done {
    def getInstance(): Done = this
    def done(): Done = this
  }

  object ActionResult {
    val done = Right(Done)
    def success[A](a: A): ActionResult[A] = Right(a)
    def error[A](message: String): ActionResult[A] = Left(Error(message))
    def error[A](e: Error): ActionResult[A] = Left(e)
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
