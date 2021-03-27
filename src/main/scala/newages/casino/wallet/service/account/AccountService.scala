package newages.casino.wallet.service.account

import cats.effect.IO
import newages.casino.wallet.domain.ActionResult
import newages.casino.wallet.model.AccountId
import newages.casino.wallet.service.GeneratorService

trait AccountService {
  def createAccount: IO[ActionResult[AccountId]]
}

object AccountService {
  def apply(generator: GeneratorService[IO, String], persistence: AccountPersistence) =
    new AccountServiceImpl(generator, persistence)
}

class AccountServiceImpl(
    generator: GeneratorService[IO, String],
    persistence: AccountPersistence
) extends AccountService {
  override def createAccount: IO[ActionResult[AccountId]] =
    generator.nextId.map(v => ActionResult.success(AccountId(v)))
}
