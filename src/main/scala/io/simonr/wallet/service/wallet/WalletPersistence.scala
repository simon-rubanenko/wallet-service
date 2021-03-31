package io.simonr.wallet.service.wallet

import cats.effect.IO
import doobie.implicits._
import io.simonr.utils.doobie.DoobiePersistence
import io.simonr.wallet.model.{AccountId, CurrencyId, WalletId}

trait WalletPersistence {
  def addWallet(walletId: WalletId): IO[Unit]

  def addAccount(
      walletId: WalletId,
      accountId: AccountId,
      currencyId: CurrencyId
  ): IO[Unit]

  def getAccountByCurrency(
      walletId: WalletId,
      currencyId: CurrencyId
  ): IO[Option[AccountId]]
}

object WalletPersistence {
  def apply(db: DoobiePersistence): WalletPersistence = new WalletPersistenceImpl(db)
}

class WalletPersistenceImpl(val db: DoobiePersistence) extends WalletPersistence {
  def addWallet(walletId: WalletId): IO[Unit] =
    sql"""insert into wallet.wallet(wallet_id) values(${walletId.id})"""
      .update
      .run
      .transact(db.autoCommitTransactor)
      .map(_ => ())

  def addAccount(
      walletId: WalletId,
      accountId: AccountId,
      currencyId: CurrencyId
  ): IO[Unit] =
    sql"""insert into wallet.wallet_account(wallet_account_wallet_id, wallet_account_account_id, wallet_account_currency_id) 
         values(${walletId.id}, ${accountId.id}, ${currencyId.id})"""
      .update
      .run
      .transact(db.autoCommitTransactor)
      .map(_ => ())

  def getAccountByCurrency(
      walletId: WalletId,
      currencyId: CurrencyId
  ): IO[Option[AccountId]] =
    sql"""select wallet_account_account_id
         from wallet.wallet_account
         where wallet_account_wallet_id = ${walletId.id}
          and wallet_account_currency_id = ${currencyId.id}"""
      .query[AccountId]
      .option
      .transact(db.autoCommitTransactor)
}
