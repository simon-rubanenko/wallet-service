package newages.casino.wallet

import newages.casino.wallet.model.AccountId
import newages.casino.wallet.model.AccountType.AccountType

package object dao {
  final case class Account(id: AccountId, amount: BigDecimal, accountType: AccountType)

}
