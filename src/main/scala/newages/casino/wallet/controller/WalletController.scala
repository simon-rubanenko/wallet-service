package newages.casino.wallet.controller

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import newages.casino.wallet.model.Amount
import newages.casino.wallet.service.account.AccountService
import newages.casino.wallet.service.user.UserService

trait WalletController {
  def registerUser(userIdStr: String): IO[Either[Error, Balance]]
  def deposit(userIdStr: String, amount: BigDecimal): IO[Either[Error, Balance]]
  def withdraw(userIdStr: String, amount: BigDecimal): IO[Either[Error, Balance]]
  def getBalance(userIdStr: String): IO[Either[Error, Balance]]
}

object WalletController {
  def apply(
      playerService: UserService,
      accountService: AccountService
  ): WalletController =
    new WalletControllerImpl(playerService, accountService)
}

class WalletControllerImpl(
    val playerService: UserService,
    val accountService: AccountService
) extends WalletController {

  override def registerUser(userIdStr: String): IO[Either[Error, Balance]] = {
    val userId = PlayerIdValidator.parse(userIdStr)
    for {
      _ <- playerService.register(userId)
      accountId <- playerService.getDefaultAccountId(userId)
      balance <- accountService.getBalance(accountId)
    } yield Right(Balance(balance.value))
  }

  override def deposit(userIdStr: String, amount: BigDecimal): IO[Either[Error, Balance]] = {
    val userId = PlayerIdValidator.parse(userIdStr)
    for {
      accountId <- playerService.getDefaultAccountId(userId)
      balance <- accountService.deposit(accountId, Amount(amount))
    } yield Balance(balance.value).asRight
  }

  override def withdraw(userIdStr: String, amount: BigDecimal): IO[Either[Error, Balance]] = {
    val userId = PlayerIdValidator.parse(userIdStr)
    (for {
      accountId <- playerService.getDefaultAccountId(userId)
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

  override def getBalance(userIdStr: String): IO[Either[Error, Balance]] = {
    val userId = PlayerIdValidator.parse(userIdStr)
    (for {
      accountId <- playerService.getDefaultAccountId(userId)
      balance <- accountService.getBalance(accountId)
    } yield balance)
      .map(v => Balance(v.value).asRight)
  }

}
