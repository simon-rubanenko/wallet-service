package newages.casino.wallet.service.user

import cats.data.OptionT
import cats.effect.IO
import newages.casino.wallet.model.{AccountId, Currency, UserId, WalletId}
import newages.casino.wallet.service.wallet.WalletService

trait UserService {
  def register(playerId: UserId): IO[Unit]
  def getDefaultAccountId(playerId: UserId): IO[AccountId]
}

object UserService {
  def apply(walletService: WalletService, persistence: UserPersistence) =
    new PlayerServiceImpl(walletService, persistence)
}

class PlayerServiceImpl(
    walletService: WalletService,
    persistence: UserPersistence
) extends UserService {
  override def register(userId: UserId): IO[Unit] =
    for {
      walletId <- walletService.createWallet
      _ <- persistence.addUser(userId, walletId)
    } yield ()

  def getDefaultAccountId(userId: UserId): IO[AccountId] = {
    def playerNotFound =
      IO.raiseError[AccountId](new Throwable(s"User [${userId.id}] not found"))

    def getDefaultAccountIdForWallet(walletId: WalletId) =
      walletService.getAccountIdByCurrency(walletId, Currency.default.id)
        .flatMap(_.fold(
          IO.raiseError[AccountId](
            new Throwable(s"Default account for wallet [${walletId.id}] not found")
          )
        )(v => IO.pure(v)))

    for {
      walletId <- persistence.getUserWalletId(userId)
      accountId <- OptionT.fromOption[IO](walletId)
        .foldF(playerNotFound)(getDefaultAccountIdForWallet)
    } yield accountId
  }
}
