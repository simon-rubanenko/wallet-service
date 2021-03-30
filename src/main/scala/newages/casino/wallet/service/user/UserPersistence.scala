package newages.casino.wallet.service.user

import cats.effect.IO
import newages.casino.wallet.model.{UserId, WalletId}
import doobie.implicits._
import newages.casino.wallet.service.DoobiePersistence

trait UserPersistence {
  def addUser(playerId: UserId, walletId: WalletId): IO[Unit]
  def getUserWalletId(playerId: UserId): IO[Option[WalletId]]
}

object UserPersistence {
  def apply(db: DoobiePersistence): UserPersistence = new PlayerPersistenceImpl(db)
}

class PlayerPersistenceImpl(val db: DoobiePersistence) extends UserPersistence {
  def addUser(userId: UserId, walletId: WalletId): IO[Unit] =
    sql"""insert into user.user(user_id, user_wallet_id) 
         values(${userId.id}, ${walletId.id})"""
      .update
      .run
      .transact(db.autoCommitTransactor)
      .map(_ => ())

  def getUserWalletId(userId: UserId): IO[Option[WalletId]] =
    sql"""select user_wallet_id from user.user
          where user_id = ${userId.id}"""
      .query[WalletId]
      .option
      .transact(db.autoCommitTransactor)
}
