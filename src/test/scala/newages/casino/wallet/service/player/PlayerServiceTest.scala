package newages.casino.wallet.service.player

import newages.casino.wallet.model.{AccountId, Currency, PlayerId, WalletId}
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
    val playerId = PlayerId("player#1")
    val walletId = WalletId("wallet#1")
    whenF(persistenceMock.addPlayer(playerId, walletId)).thenReturn(())
    whenF(walletServiceMock.createWallet).thenReturn(walletId)
    val playerService = PlayerService(walletServiceMock, persistenceMock)
    playerService
      .register(playerId)
      .unsafeRunSync() shouldEqual ()
    verify(persistenceMock, times(1)).addPlayer(playerId, walletId)
    verify(walletServiceMock, times(1)).createWallet
  }

  test("should return default account id for player") {
    val playerId = PlayerId("player#1")
    val walletId = WalletId("wallet#1")
    val accountId = AccountId("acc#1")
    val currencyId = Currency.default.id
    whenF(persistenceMock.addPlayer(playerId, walletId)).thenReturn(())
    whenF(persistenceMock.getPlayerWalletId(any[PlayerId])).thenAnswer { id: PlayerId =>
      id match {
        case p: PlayerId if p == playerId => Some(walletId)
        case _                            => None
      }
    }
    whenF(walletServiceMock.createWallet).thenReturn(walletId)
    whenF(walletServiceMock.getAccountIdByCurrency(walletId, currencyId)).thenReturn(
      Some(accountId)
    )
    val playerService = PlayerService(walletServiceMock, persistenceMock)
    (for {
      _ <- playerService.register(playerId)
      accountId <- playerService.getDefaultAccountId(playerId)
    } yield accountId)
      .unsafeRunSync() shouldEqual Some(accountId)

    assertThrows[Throwable] {
      playerService
        .getDefaultAccountId(PlayerId("unknown player id"))
        .unsafeRunSync()
    }
  }

}
