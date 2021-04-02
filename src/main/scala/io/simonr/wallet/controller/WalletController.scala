package io.simonr.wallet.controller

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import io.simonr.wallet.model.Amount
import io.simonr.wallet.service.account.AccountService
import io.simonr.wallet.service.user.UserService

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
    (for {
      _ <- playerService.register(userId)
      accountId <- playerService.getDefaultAccountId(userId)
      balance <- accountService.getBalance(accountId)
    } yield balance)
      .attempt
      .map(proceedAttempt)
  }

  override def deposit(userIdStr: String, amount: BigDecimal): IO[Either[Error, Balance]] = {
    val userId = PlayerIdValidator.parse(userIdStr)
    (for {
      accountId <- playerService.getDefaultAccountId(userId)
      balance <- accountService.deposit(accountId, Amount(amount))
    } yield balance)
      .attempt
      .map(proceedAttempt)
  }

  override def withdraw(userIdStr: String, amount: BigDecimal): IO[Either[Error, Balance]] = {
    val userId = PlayerIdValidator.parse(userIdStr)
    (for {
      accountId <- playerService.getDefaultAccountId(userId)
      balance <- accountService.withdraw(accountId, Amount(amount))
    } yield balance)
      .attempt
      .map(proceedAttempt)
  }

  override def getBalance(userIdStr: String): IO[Either[Error, Balance]] = {
    val userId = PlayerIdValidator.parse(userIdStr)
    (for {
      accountId <- playerService.getDefaultAccountId(userId)
      balance <- accountService.getBalance(accountId)
    } yield balance)
      .attempt
      .map(proceedAttempt)
  }

  private def proceedAttempt(attempt: Either[Throwable, Amount]): Either[Error, Balance] =
    attempt match {
      case Left(e)  => Error(e.getMessage).asLeft
      case Right(v) => Balance(v.value).asRight
    }

}
