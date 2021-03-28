package newages.casino.wallet

import cats.effect.{ExitCode, IO, IOApp}
import newages.casino.wallet.service.account.{AccountPersistence, AccountService}
import newages.casino.wallet.service.SimpleIncrementalGeneratorService
import newages.casino.wallet.service.player.{PlayerPersistence, PlayerService}
import newages.casino.wallet.service.wallet.{WalletPersistence, WalletService}

object Server extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      generator <- SimpleIncrementalGeneratorService.makeRef
      accountPersistence = AccountPersistence()
      accountService = AccountService(generator, accountPersistence)
      walletPersistence = WalletPersistence()
      walletService = WalletService(accountService, walletPersistence)
      playerPersistence = PlayerPersistence()
      playerService = PlayerService(playerPersistence)
    } yield ExitCode.Success
}
