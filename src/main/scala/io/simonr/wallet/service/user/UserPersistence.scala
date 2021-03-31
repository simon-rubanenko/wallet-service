package io.simonr.wallet.service.user

import cats.effect.IO
import doobie.implicits._
import io.simonr.utils.doobie.DoobiePersistence
import io.simonr.wallet.model.{UserId, WalletId}

trait UserPersistence {
  def addUser(playerId: UserId, walletId: WalletId): IO[Unit]
  def getUserWalletId(playerId: UserId): IO[Option[WalletId]]
}

object UserPersistence {
  def apply(db: DoobiePersistence): UserPersistence = new PlayerPersistenceImpl(db)
}

class PlayerPersistenceImpl(val db: DoobiePersistence) extends UserPersistence {
  def addUser(userId: UserId, walletId: WalletId): IO[Unit] =
    sql"""insert into wallet_user.user(user_id, user_wallet_id) 
         values(${userId.id}, ${walletId.id})"""
      .update
      .run
      .transact(db.autoCommitTransactor)
      .map(_ => ())

  def getUserWalletId(userId: UserId): IO[Option[WalletId]] =
    sql"""select user_wallet_id from wallet_user.user
          where user_id = ${userId.id}"""
      .query[WalletId]
      .option
      .transact(db.autoCommitTransactor)
}
