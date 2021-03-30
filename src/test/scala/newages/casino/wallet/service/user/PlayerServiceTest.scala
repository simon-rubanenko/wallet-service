package newages.casino.wallet.service.user

import newages.casino.wallet.model.{AccountId, Currency, UserId, WalletId}
import newages.casino.wallet.service.wallet.WalletService
import org.mockito.cats.MockitoCats.whenF
import org.mockito.scalatest.{MockitoSugar, ResetMocksAfterEachTest}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class PlayerServiceTest
    extends AnyFunSuite
    with MockitoSugar
    with ResetMocksAfterEachTest
    with Matchers {

  val persistenceMock: PlayerPersistence = mock[PlayerPersistence]
  val walletServiceMock: WalletService = mock[WalletService]

  test("should create player with wallet") {
    val playerId = UserId("player#1")
    val walletId = WalletId("wallet#1")
    whenF(persistenceMock.addPlayer(playerId, walletId)).thenReturn(())
    whenF(walletServiceMock.createWallet).thenReturn(walletId)
    val playerService = UserService(walletServiceMock, persistenceMock)
    playerService
      .register(playerId)
      .unsafeRunSync() shouldEqual ()
    verify(persistenceMock, times(1)).addPlayer(playerId, walletId)
    verify(walletServiceMock, times(1)).createWallet
  }

  test("should return default account id for player") {
    val playerId = UserId("player#1")
    val walletId = WalletId("wallet#1")
    val accountId = AccountId("acc#1")
    val currencyId = Currency.default.id
    whenF(persistenceMock.addPlayer(playerId, walletId)).thenReturn(())
    whenF(persistenceMock.getUserWalletId(playerId)).thenReturn(Some(walletId))
    whenF(walletServiceMock.createWallet).thenReturn(walletId)
    whenF(walletServiceMock.getAccountIdByCurrency(walletId, currencyId)).thenReturn(
      Some(accountId)
    )
    val playerService = UserService(walletServiceMock, persistenceMock)
    (for {
      _ <- playerService.register(playerId)
      accountId <- playerService.getDefaultAccountId(playerId)
    } yield accountId)
      .unsafeRunSync() shouldEqual Some(accountId)
  }

  test("should throw Throwable id player not found") {
    val playerId = UserId("player#2")
    val walletId = WalletId("wallet#2")
    whenF(persistenceMock.addPlayer(playerId, walletId)).thenReturn(())
    whenF(persistenceMock.getUserWalletId(any[UserId])).thenReturn(None)
    whenF(walletServiceMock.createWallet).thenReturn(walletId)
    val playerService = UserService(walletServiceMock, persistenceMock)
    assertThrows[Throwable] {
      (for {
        _ <- playerService.register(playerId)
        _ <- playerService.getDefaultAccountId(UserId("unknown player id"))
      } yield ())
        .unsafeRunSync()
    }
  }

}
