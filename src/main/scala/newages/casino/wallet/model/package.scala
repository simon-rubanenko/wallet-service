package newages.casino.wallet

package object model {

  object AccountType extends Enumeration {
    type AccountType = Value
    val User, Casino = Value
  }

  final case class UserId(id: String) extends AnyVal

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

  case class Amount(value: BigDecimal) extends AnyVal {
    def +(other: Amount): Amount = copy(value = value + other.value)
    def -(other: Amount): Amount = copy(value = value - other.value)
  }

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

  final case class PlayerBalance(playerId: UserId, balance: Money)

  final case class TransactionId(id: String) extends AnyVal

  final case class CurrencyId(id: String) extends AnyVal {
    override def toString: String = id
  }
  final case class Currency(id: CurrencyId, code: String)

  object Currency {
    val USD: Currency = Currency(CurrencyId("840"), "USD")
    val default: Currency = USD
  }

  final case class PlayerInfo(playerId: UserId, defaultAccount: Money)

  final case class AccountInfo(balance: Amount, currencyId: CurrencyId)
}
