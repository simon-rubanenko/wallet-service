package newages.casino.wallet.controller

import cats.effect.IO

trait WalletController {
  def registerPlayer(playerId: String): IO[Either[Error, Balance]]
  def deposit(playerId: String, amount: BigDecimal): IO[Either[Error, Balance]]
  def withdraw(playerId: String, amount: BigDecimal): IO[Either[Error, Balance]]
}

object WalletController {
  def apply(): WalletController = new WalletControllerImpl()
}

class WalletControllerImpl extends WalletController {

  override def registerPlayer(playerId: String): IO[Either[Error, Balance]] = ???

  override def deposit(playerId: String, amount: BigDecimal): IO[Either[Error, Balance]] = ???

  override def withdraw(playerId: String, amount: BigDecimal): IO[Either[Error, Balance]] = ???

}
