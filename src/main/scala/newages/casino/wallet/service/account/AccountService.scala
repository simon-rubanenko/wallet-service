package newages.casino.wallet.service.account

import cats.effect.IO
import newages.casino.wallet.model.{AccountId, Amount}
import newages.casino.wallet.service.GeneratorService

trait AccountService {
  def createAccount: IO[AccountId]
  def getBalance(accountId: AccountId): IO[Amount]
  def deposit(accountId: AccountId, amount: Amount): IO[Amount]
  def withdraw(accountId: AccountId, amount: Amount): IO[Amount]
}

object AccountService {
  def apply(
      generator: GeneratorService[IO, String],
      persistence: AccountPersistence
  ): AccountService =
    new AccountServiceImpl(generator, persistence)
}

class AccountServiceImpl(
    generator: GeneratorService[IO, String],
    persistence: AccountPersistence
) extends AccountService {
  override def createAccount: IO[AccountId] =
    (for {
      id <- generator.nextId
      _ <- persistence.addAccount(AccountId(id))
    } yield id)
      .map(AccountId)

  override def getBalance(accountId: AccountId): IO[Amount] =
    persistence.getBalance(accountId)

  def deposit(accountId: AccountId, amount: Amount): IO[Amount] =
    persistence.deposit(accountId, amount)

  def withdraw(accountId: AccountId, amount: Amount): IO[Amount] =
    persistence.withdraw(accountId, amount)

}
