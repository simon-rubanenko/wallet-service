package newages.casino.wallet.service.user

import cats.data.OptionT
import cats.effect.IO
import newages.casino.wallet.model.{AccountId, Currency, PlayerId, WalletId}
import newages.casino.wallet.service.wallet.WalletService

trait PlayerService {
  def register(playerId: PlayerId): IO[Unit]
  def getDefaultAccountId(playerId: PlayerId): IO[AccountId]
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

  def getDefaultAccountId(playerId: PlayerId): IO[AccountId] = {
    def playerNotFound =
      IO.raiseError[AccountId](new Throwable(s"Player [${playerId.id}] not found"))

    def getDefaultAccountIdForWallet(walletId: WalletId) =
      walletService.getAccountIdByCurrency(walletId, Currency.default.id)
        .flatMap(_.fold(
          IO.raiseError[AccountId](
            new Throwable(s"Default account for wallet [${walletId.id}] not found")
          )
        )(v => IO.pure(v)))

    for {
      walletId <- persistence.getPlayerWalletId(playerId)
      accountId <- OptionT.fromOption[IO](walletId)
        .foldF(playerNotFound)(getDefaultAccountIdForWallet)
    } yield accountId
  }
}
