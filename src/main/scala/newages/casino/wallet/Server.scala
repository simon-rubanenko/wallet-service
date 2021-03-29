package newages.casino.wallet

import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.config.ConfigFactory
import newages.casino.wallet.persistence.DoobiePersistence
import newages.casino.wallet.service.account.{AccountPersistence, AccountService}
import newages.casino.wallet.service.SimpleIncrementalGeneratorService
import newages.casino.wallet.service.player.{PlayerPersistence, PlayerService}
import newages.casino.wallet.service.wallet.{WalletPersistence, WalletService}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object Server extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      generator <- SimpleIncrementalGeneratorService.makeRef

      cf <- IO.delay(ConfigFactory.defaultApplication())
      dbContext <- IO.delay(
        ExecutionContext.fromExecutor(Executors.newFixedThreadPool(cf.getInt("doobie.threads")))
      )

      db = DoobiePersistence(
        cf.getString("doobie.url"),
        cf.getString("doobie.user"),
        cf.getString("doobie.pass")
      )(dbContext)

      accountPersistence = AccountPersistence(db)
      walletPersistence = WalletPersistence(db)
      playerPersistence = PlayerPersistence(db)

      accountService = AccountService(generator, accountPersistence)
      walletService = WalletService(generator, accountService, walletPersistence)
      playerService = PlayerService(playerPersistence)
    } yield ExitCode.Success
}
