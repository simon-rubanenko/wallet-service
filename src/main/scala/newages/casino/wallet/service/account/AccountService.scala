package newages.casino.wallet.service.account

import cats.effect.IO
import newages.casino.wallet.domain.{ActionResult, Done}
import newages.casino.wallet.model.AccountId
import newages.casino.wallet.service.GeneratorService

trait AccountService {
  def createAccount: IO[ActionResult[AccountId]]
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
  override def createAccount: IO[ActionResult[AccountId]] =
    (for {
      id <- generator.nextId
      result <- persistence.addAccount(AccountId(id))
    } yield (result, id))
      .map {
        case (Right(Done), id) => ActionResult.success(AccountId(id))
        case (Left(e), _)      => ActionResult.error(e)
      }
}
