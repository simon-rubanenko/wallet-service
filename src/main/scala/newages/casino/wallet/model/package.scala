package newages.casino.wallet

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

  final case class Money(value: BigDecimal) extends AnyVal {
    def >=(other: Money): Boolean = value >= other.value
    def +(other: Money): Money = Money(value + other.value)
    def -(other: Money): Money = Money(value - other.value)

    override def toString: String = value.toString()
  }

  object Money {
    val Zero: Money = Money(0.0)
  }

  final case class PlayerBalance(playerId: PlayerId, balance: Money)

  final case class TransactionId(id: String) extends AnyVal

  final case class CurrencyId(id: String) extends AnyVal

}
