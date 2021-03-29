package newages.casino.wallet.service.account

import cats.effect.IO
import newages.casino.wallet.model.AccountId
import newages.casino.wallet.service.GeneratorService

trait AccountService {
  def createAccount: IO[AccountId]
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
}
