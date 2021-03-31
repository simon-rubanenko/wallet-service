package io.simonr.wallet.service.account

import cats.effect.IO
import io.simonr.wallet.model.{AccountId, Amount}
import io.simonr.wallet.service.GeneratorService
import org.mockito.cats.MockitoCats.whenF
import org.mockito.scalatest.{MockitoSugar, ResetMocksAfterEachTest}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class AccountServiceTest
    extends AnyFunSuite
    with MockitoSugar
    with ResetMocksAfterEachTest
    with Matchers {

  val persistenceMock: AccountPersistence = mock[AccountPersistence]
  val generatorMock: GeneratorService[IO, String] = mock[GeneratorService[IO, String]]

  test("should add new account") {
    val accountId = AccountId("acc#1")
    whenF(generatorMock.nextId).thenReturn(accountId.id)
    whenF(persistenceMock.addAccount(accountId)).thenReturn(())
    val accountService = AccountService(generatorMock, persistenceMock)
    accountService
      .createAccount
      .unsafeRunSync() shouldEqual accountId
    verify(persistenceMock, times(1)).addAccount(accountId)
  }

  test("should catch exception by attempt to update db") {
    val accountId = AccountId("acc#1")
    val errorMessage = "some error message"
    whenF(generatorMock.nextId).thenReturn(accountId.id)
    when(persistenceMock.addAccount(accountId)).thenReturn(IO.raiseError(new Throwable))
    val accountService = AccountService(generatorMock, persistenceMock)
    assertThrows[Throwable] {
      accountService
        .createAccount
        .unsafeRunSync()
    }
    verify(persistenceMock, times(1)).addAccount(accountId)
  }

  test("should got balance") {
    val accountId = AccountId("acc#1")
    val amount = Amount(12.34)
    whenF(persistenceMock.getBalance(accountId)).thenReturn(amount)
    val accountService = AccountService(generatorMock, persistenceMock)
    accountService
      .getBalance(accountId)
      .unsafeRunSync() shouldEqual amount
    verify(persistenceMock, times(1)).getBalance(accountId)
  }
}
