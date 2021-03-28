package newages.casino.wallet.service.player

import cats.effect.IO
import newages.casino.wallet.domain.{ActionResult, Done}
import newages.casino.wallet.model.{AccountId, PlayerId}
import newages.casino.wallet.service.GeneratorService

trait PlayerService {
  def register(playerId: PlayerId): IO[ActionResult[Done]]
}

object PlayerService {
  def apply(persistence: PlayerPersistence) =
    new PlayerServiceImpl(persistence)
}

class PlayerServiceImpl(
    persistence: PlayerPersistence
) extends PlayerService {
  override def register(playerId: PlayerId): IO[ActionResult[Done]] = ???
}
