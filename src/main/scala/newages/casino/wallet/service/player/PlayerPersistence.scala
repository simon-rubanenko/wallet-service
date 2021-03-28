package newages.casino.wallet.service.player

import cats.effect.IO
import newages.casino.wallet.domain.{ActionResult, Done}
import newages.casino.wallet.model.PlayerId

trait PlayerPersistence {
  def addPlayer(playerId: PlayerId): IO[ActionResult[Done]]
  def getPlayer(playerId: PlayerId): IO[ActionResult[Option[PlayerInfoDao]]]
}

object PlayerPersistence {
  def apply(): PlayerPersistence = new PlayerPersistenceImpl()
}

class PlayerPersistenceImpl extends PlayerPersistence {
  def addPlayer(playerId: PlayerId): IO[ActionResult[Done]] = ???

  def getPlayer(playerId: PlayerId): IO[ActionResult[Option[PlayerInfoDao]]] = ???
}

final case class PlayerInfoDao(playerId: PlayerId, active: Boolean)
