package newages.casino.wallet.service.player

import cats.data.OptionT
import cats.effect.IO
import newages.casino.wallet.model.{AccountId, Currency, PlayerId, WalletId}
import newages.casino.wallet.service.wallet.WalletService

trait PlayerService {
  def register(playerId: PlayerId): IO[Unit]
  def getDefaultAccountId(playerId: PlayerId): IO[Option[AccountId]]
}

object PlayerService {
  def apply(walletService: WalletService, persistence: PlayerPersistence) =
    new PlayerServiceImpl(walletService, persistence)
}

class PlayerServiceImpl(
    walletService: WalletService,
    persistence: PlayerPersistence
) extends PlayerService {
  override def register(playerId: PlayerId): IO[Unit] =
    for {
      walletId <- walletService.createWallet
      _ <- persistence.addPlayer(playerId, walletId)
    } yield ()

  def getDefaultAccountId(playerId: PlayerId): IO[Option[AccountId]] = {
    def playerNotFound =
      IO.raiseError[Option[AccountId]](new Throwable(s"Player [${playerId.id}] not found"))

    def getAccountId(walletId: WalletId) =
      walletService.getAccountIdByCurrency(walletId, Currency.default.id)

    for {
      walletId <- persistence.getPlayerWalletId(playerId)
      accountId <- OptionT.fromOption[IO](walletId)
        .foldF(playerNotFound)(getAccountId)
    } yield accountId
  }
}
