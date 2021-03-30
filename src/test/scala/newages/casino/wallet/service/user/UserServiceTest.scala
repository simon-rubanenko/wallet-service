package newages.casino.wallet.service.user

import newages.casino.wallet.model.{AccountId, Currency, UserId, WalletId}
import newages.casino.wallet.service.wallet.WalletService
import org.mockito.cats.MockitoCats.whenF
import org.mockito.scalatest.{MockitoSugar, ResetMocksAfterEachTest}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class UserServiceTest
    extends AnyFunSuite
    with MockitoSugar
    with ResetMocksAfterEachTest
    with Matchers {

  val persistenceMock: UserPersistence = mock[UserPersistence]
  val walletServiceMock: WalletService = mock[WalletService]

  test("should create user with wallet") {
    val userId = UserId("user#1")
    val walletId = WalletId("wallet#1")
    whenF(persistenceMock.addUser(userId, walletId)).thenReturn(())
    whenF(walletServiceMock.createWallet).thenReturn(walletId)
    val playerService = UserService(walletServiceMock, persistenceMock)
    playerService
      .register(userId)
      .unsafeRunSync() shouldEqual ()
    verify(persistenceMock, times(1)).addUser(userId, walletId)
    verify(walletServiceMock, times(1)).createWallet
  }

  test("should return default account id for user") {
    val userId = UserId("user#1")
    val walletId = WalletId("wallet#1")
    val accountId = AccountId("acc#1")
    val currencyId = Currency.default.id
    whenF(persistenceMock.addUser(userId, walletId)).thenReturn(())
    whenF(persistenceMock.getUserWalletId(userId)).thenReturn(Some(walletId))
    whenF(walletServiceMock.createWallet).thenReturn(walletId)
    whenF(walletServiceMock.getAccountIdByCurrency(walletId, currencyId)).thenReturn(
      Some(accountId)
    )
    val userService = UserService(walletServiceMock, persistenceMock)
    (for {
      _ <- userService.register(userId)
      accountId <- userService.getDefaultAccountId(userId)
    } yield accountId)
      .unsafeRunSync() shouldEqual accountId
  }

  test("should throw Throwable id user not found") {
    val userId = UserId("user#2")
    val walletId = WalletId("wallet#2")
    whenF(persistenceMock.addUser(userId, walletId)).thenReturn(())
    whenF(persistenceMock.getUserWalletId(any[UserId])).thenReturn(None)
    whenF(walletServiceMock.createWallet).thenReturn(walletId)
    val userService = UserService(walletServiceMock, persistenceMock)
    assertThrows[Throwable] {
      (for {
        _ <- userService.register(userId)
        _ <- userService.getDefaultAccountId(UserId("unknown user id"))
      } yield ())
        .unsafeRunSync()
    }
  }

}
