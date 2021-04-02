package io.simonr.wallet.service.wallet

import cats.effect.{ContextShift, IO}
import io.simonr.wallet.model.{AccountId, Currency, CurrencyId, WalletId}
import io.simonr.wallet.service.GeneratorService
import io.simonr.wallet.service.account.AccountService
import org.mockito.cats.MockitoCats.whenF
import org.mockito.scalatest.{MockitoSugar, ResetMocksAfterEachTest}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class WalletServiceTest
    extends AnyFunSuite
    with MockitoSugar
    with ResetMocksAfterEachTest
    with Matchers {

  val persistenceMock: WalletPersistence = mock[WalletPersistence]
  val generatorMock: GeneratorService[IO, String] = mock[GeneratorService[IO, String]]
  val accountServiceMock: AccountService = mock[AccountService]

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  test("should create wallet with account") {
    val walletId = WalletId("wallet#1")
    val accountId = AccountId("acc1")
    val currencyId = Currency.default.id
    whenF(generatorMock.nextId).thenReturn(walletId.id)
    whenF(persistenceMock.addWallet(walletId)).thenReturn(())
    whenF(persistenceMock.addAccount(walletId, accountId, currencyId)).thenReturn(())
    whenF(accountServiceMock.createAccount).thenReturn(accountId)
    val walletService = WalletService(generatorMock, accountServiceMock, persistenceMock)
    walletService
      .createWallet
      .unsafeRunSync() shouldEqual walletId
    verify(persistenceMock, times(1)).addWallet(walletId)
    verify(accountServiceMock, times(1)).createAccount
    verify(persistenceMock, times(1)).addAccount(walletId, accountId, currencyId)
  }

  test("should return account by currency id") {
    val walletId = WalletId("wallet#1")
    val accountId = AccountId("acc1")
    val currencyId = Currency.default.id
    whenF(generatorMock.nextId).thenReturn(walletId.id)
    whenF(persistenceMock.addWallet(walletId)).thenReturn(())
    whenF(persistenceMock.addAccount(walletId, accountId, currencyId)).thenReturn(())
    whenF(accountServiceMock.createAccount).thenReturn(accountId)
    whenF(persistenceMock.getAccountByCurrency(eqTo(walletId), any[CurrencyId])).thenAnswer(
      (_: WalletId, curId: CurrencyId) =>
        curId match {
          case Currency.default.id => Some(accountId)
          case _                   => None
        }
    )
    val walletService = WalletService(generatorMock, accountServiceMock, persistenceMock)
    val (acc1, acc2) =
      (for {
        walletId <- walletService.createWallet
        acc1 <- walletService.getAccountIdByCurrency(walletId, currencyId)
        acc2 <- walletService.getAccountIdByCurrency(walletId, CurrencyId("unknown currency id"))
      } yield (acc1, acc2))
        .unsafeRunSync()

    acc1 shouldEqual Some(accountId)
    acc2 shouldEqual None
  }

}
