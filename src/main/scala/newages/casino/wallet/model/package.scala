package newages.casino.wallet

import akka.Done

package object model {

  object AccountType extends Enumeration {
    type AccountType = Value
    val User, Casino = Value
  }

  final case class PlayerId(id: String) extends AnyVal

  final case class AccountId(id: String) extends AnyVal {
    override def toString: String = id
  }

  final case class PaymentTypeId(typeId: String) extends AnyVal {
    override def toString: String = typeId
  }

  final case class Payment(
      accountFrom: AccountId,
      accountTo: AccountId,
      amount: Money,
      paymentTypeId: PaymentTypeId
  )

  final case class WalletId(id: String) extends AnyVal {
    override def toString: String = id
  }

  case class Amount(value: BigDecimal) extends AnyVal

  final case class Money(amount: Amount, currency: Currency) {
    /*
    def >=(other: Money): Boolean = value >= other.value
    def +(other: Money): Money = Money(value + other.value)
    def -(other: Money): Money = Money(value - other.value)
     */

    override def toString: String = s"${amount.value} ${currency.code}"
  }

  object Money {
    def Zero(currency: Currency = Currency.default): Money = Money(Amount(0.0), currency)
  }

  final case class PlayerBalance(playerId: PlayerId, balance: Money)

  final case class TransactionId(id: String) extends AnyVal

  final case class CurrencyId(id: String) extends AnyVal {
    override def toString: String = id
  }
  final case class Currency(id: CurrencyId, code: String)

  object Currency {
    val USD: Currency = Currency(CurrencyId("840"), "USD")
    val default: Currency = USD
  }

  final case class AccountInfo(balance: Amount, currencyId: CurrencyId)

  type ActionResult[T] = Either[Throwable, T]

  object ActionResult {
    def error[T](message: String): ActionResult[T] = Left(new Throwable(message))
    def done: ActionResult[Done] = Right(Done)
    def success[T](a: T): ActionResult[T] = Right(a)
  }

}
