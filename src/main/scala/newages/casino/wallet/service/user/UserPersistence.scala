package newages.casino.wallet.service.user

import cats.effect.IO
import newages.casino.wallet.model.{PlayerId, WalletId}
import doobie.implicits._
import newages.casino.wallet.service.DoobiePersistence

trait PlayerPersistence {
  def addPlayer(playerId: PlayerId, walletId: WalletId): IO[Unit]
  def getPlayerWalletId(playerId: PlayerId): IO[Option[WalletId]]
}

object PlayerPersistence {
  def apply(db: DoobiePersistence): PlayerPersistence = new PlayerPersistenceImpl(db)
}

class PlayerPersistenceImpl(val db: DoobiePersistence) extends PlayerPersistence {
  def addPlayer(playerId: PlayerId, walletId: WalletId): IO[Unit] =
    sql"""insert into player.player(player_id, player_wallet_id) 
         values(${playerId.id}, ${walletId.id})"""
      .update
      .run
      .transact(db.autoCommitTransactor)
      .map(_ => ())

  def getPlayerWalletId(playerId: PlayerId): IO[Option[WalletId]] =
    sql"""select player_wallet_id from player.player
          where player_id = ${playerId.id}"""
      .query[WalletId]
      .option
      .transact(db.autoCommitTransactor)
}
