package io.simonr.wallet.service.wallet

import cats.effect.IO
import io.simonr.wallet.model.{AccountId, Currency, CurrencyId, WalletId}
import io.simonr.wallet.service.GeneratorService
import io.simonr.wallet.service.account.AccountService

trait WalletService {
  def createWallet: IO[WalletId]

  def getAccountIdByCurrency(walletId: WalletId, currencyId: CurrencyId): IO[Option[AccountId]]
}

object WalletService {
  def apply(
      generator: GeneratorService[IO, String],
      accountService: AccountService,
      persistence: WalletPersistence
  ): WalletService =
    new WalletServiceImpl(generator, accountService, persistence)
}

class WalletServiceImpl(
    generator: GeneratorService[IO, String],
    accountService: AccountService,
    persistence: WalletPersistence
) extends WalletService {

  private val currency = Currency.default

  def createWallet: IO[WalletId] =
    for {
      id <- generator.nextId
      walletId = WalletId(id)
      _ <- persistence.addWallet(walletId)
      accountId <- accountService.createAccount
      _ <- persistence.addAccount(walletId, accountId, currency.id)
    } yield walletId

  def getAccountIdByCurrency(walletId: WalletId, currencyId: CurrencyId): IO[Option[AccountId]] =
    persistence.getAccountByCurrency(walletId, currencyId)
}
