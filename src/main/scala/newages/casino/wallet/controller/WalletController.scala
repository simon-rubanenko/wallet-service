package newages.casino.wallet.controller

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import newages.casino.wallet.model.Amount
import newages.casino.wallet.service.account.AccountService
import newages.casino.wallet.service.player.PlayerService

trait WalletController {
  def registerPlayer(playerId: String): IO[Either[Error, Balance]]
  def deposit(playerId: String, amount: BigDecimal): IO[Either[Error, Balance]]
  def withdraw(playerId: String, amount: BigDecimal): IO[Either[Error, Balance]]
  def getBalance(playerId: String): IO[Either[Error, Balance]]
}

object WalletController {
  def apply(): WalletController = ???
  def apply(
      playerService: PlayerService,
      accountService: AccountService
  ): WalletController =
    new WalletControllerImpl(playerService, accountService)
}

class WalletControllerImpl(
    val playerService: PlayerService,
    val accountService: AccountService
) extends WalletController {

  override def registerPlayer(playerIdStr: String): IO[Either[Error, Balance]] = {
    val playerId = PlayerIdValidator.parse(playerIdStr)
    for {
      _ <- playerService.register(playerId)
      accountId <- playerService.getDefaultAccountId(playerId)
      balance <- accountService.getBalance(accountId)
    } yield Right(Balance(balance.value))
  }

  override def deposit(playerIdStr: String, amount: BigDecimal): IO[Either[Error, Balance]] = {
    val playerId = PlayerIdValidator.parse(playerIdStr)
    for {
      accountId <- playerService.getDefaultAccountId(playerId)
      balance <- accountService.deposit(accountId, Amount(amount))
    } yield Balance(balance.value).asRight
  }

  override def withdraw(playerIdStr: String, amount: BigDecimal): IO[Either[Error, Balance]] = {
    val playerId = PlayerIdValidator.parse(playerIdStr)
    (for {
      accountId <- playerService.getDefaultAccountId(playerId)
      balance <- accountService.getBalance(accountId)
      _ <-
        if (balance.value < amount) IO.raiseError(new Throwable("Insufficient funds")) else IO.unit
      newBalance <- accountService.withdraw(accountId, Amount(amount))
    } yield newBalance)
      .attempt
      .map {
        case Left(e)  => Error(e.getMessage).asLeft
        case Right(v) => Balance(v.value).asRight
      }
  }

  override def getBalance(playerId: String): IO[Either[Error, Balance]] = ???

}
