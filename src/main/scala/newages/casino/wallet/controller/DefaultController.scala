package newages.casino.wallet.controller

import akka.Done
import akka.actor.typed.{ActorRef, Scheduler}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.util.Timeout
import newages.casino.wallet.model.{AccountId, Currency, WalletId}
import newages.casino.wallet.persistence.account.AccountCommands
import newages.casino.wallet.persistence.account.AccountCommands.AccountCommand
import newages.casino.wallet.persistence.transaction.TransactionCommands.TransactionCommand
import newages.casino.wallet.persistence.wallet.WalletCommands
import newages.casino.wallet.persistence.wallet.WalletCommands.{WalletCommand, WalletDetails}
import newages.casino.wallet.service.{AccountService, GeneratorService}

import scala.concurrent.{ExecutionContext, Future}

trait DefaultController {
  def createWallet: Future[Either[Throwable, WalletId]]
}

class DefaultControllerImpl(
    val generatorService: GeneratorService,
    val accountService: AccountService,
    val wallet: ActorRef[WalletCommand],
    val transaction: ActorRef[TransactionCommand]
)(implicit ec: ExecutionContext, timeout: Timeout, scheduler: Scheduler)
    extends DefaultController {
  override def createWallet: Future[Either[Throwable, WalletId]] = {
    val accountId = AccountId(generatorService.nextId)
//    val account = accountService.getAccountActor(accountId)
//    (for {
//      r1 <- account.ask(AccountCommands.CreateAccount)
//      r2 <- wallet.ask(WalletCommands.CreateWallet)
//      r3 <- wallet.ask(WalletCommands.AddAccount(accountId, Currency.default.id, _))
//    } yield (r1, r2, r3))
//      .map {
//        case (Right(Done), Right(Done), Right(WalletDetails(_))) =>
//          Right()
//        case _ =>
//
//      }

    Future(Right(WalletId("")))
  }
}
