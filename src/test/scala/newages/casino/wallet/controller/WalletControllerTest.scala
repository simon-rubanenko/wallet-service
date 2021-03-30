package newages.casino.wallet.controller

import cats.implicits.catsSyntaxEitherId
import newages.casino.wallet.model.{AccountId, Amount, PlayerId}
import newages.casino.wallet.service.account.AccountService
import newages.casino.wallet.service.player.PlayerService
import org.mockito.cats.MockitoCats.whenF
import org.mockito.scalatest.{MockitoSugar, ResetMocksAfterEachTest}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class WalletControllerTest
    extends AnyFunSuite
    with Matchers
    with MockitoSugar
    with ResetMocksAfterEachTest {

  val playerService: PlayerService = mock[PlayerService]
  val accountService: AccountService = mock[AccountService]

  test("should register player") {
    val playerId = PlayerId("player#1")
    val accountId = AccountId("acc#1")
    val balance = Amount(12.34)
    whenF(playerService.register(playerId)).thenReturn(())
    whenF(playerService.getDefaultAccountId(playerId)).thenReturn(accountId)
    whenF(accountService.getBalance(accountId)).thenReturn(balance)

    val walletController = WalletController(playerService, accountService)
    val result = walletController
      .registerPlayer(playerId.id)
      .unsafeRunSync()
    result shouldEqual Balance(balance.value).asRight

    verify(playerService, times(1)).register(playerId)
    verify(playerService, times(1)).getDefaultAccountId(playerId)
    verify(accountService, times(1)).getBalance(accountId)
  }

  test("should withdraw funds") {
    val playerId = PlayerId("player#1")
    val accountId = AccountId("acc#1")
    val amount = Amount(12.34)
    whenF(playerService.getDefaultAccountId(playerId)).thenReturn(accountId)
    whenF(accountService.getBalance(accountId)).thenReturn(amount)
    whenF(accountService.withdraw(accountId, amount)).thenReturn(Amount(0.0))

    val walletController = WalletController(playerService, accountService)
    val result = walletController
      .withdraw(playerId.id, amount.value)
      .unsafeRunSync()
    result shouldEqual Balance(0.0).asRight

    verify(playerService, times(1)).getDefaultAccountId(playerId)
    verify(accountService, times(1)).withdraw(accountId, amount)
  }

  test("should throw Insufficient funds") {
    val playerId = PlayerId("player#1")
    val accountId = AccountId("acc#1")
    val amount = Amount(12.34)
    whenF(playerService.getDefaultAccountId(playerId)).thenReturn(accountId)
    whenF(accountService.getBalance(accountId)).thenReturn(amount.copy(value = amount.value - 1.0))

    val walletController = WalletController(playerService, accountService)
    val result = walletController
      .withdraw(playerId.id, amount.value)
      .unsafeRunSync()
    result shouldEqual Error("Insufficient funds").asLeft

    verify(playerService, times(1)).getDefaultAccountId(playerId)
    verify(accountService, times(1)).getBalance(accountId)
  }

}
