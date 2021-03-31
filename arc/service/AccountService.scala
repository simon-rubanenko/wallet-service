package newages.casino.wallet.service

import io.simonr.wallet.service.account.AccountService
import newages.casino.wallet.model.{AccountId, ActionResult}
import newages.casino.wallet.persistence.account.AccountCommands.{AccountCommand, CreateAccount}
import newages.casino.wallet.persistence.account.AccountEntity

import scala.concurrent.Future

trait AccountService {
  def createAccount(accountId: AccountId): Future[ActionResult[Done]]
}

class AccountServiceImpl(implicit actorSystem: ActorSystem[_], timeout: Timeout)
    extends AccountService {

  val sharding: ClusterSharding = ClusterSharding(actorSystem)

  sharding.init(Entity(AccountEntity.EntityKey) { context =>
    AccountEntity(AccountId(context.entityId))
  })

  override def createAccount(accountId: AccountId): Future[ActionResult[Done]] = {
    val actor = sharding
      .entityRefFor(AccountEntity.EntityKey, accountId.id)
    actor.ask(CreateAccount)
  }

}
