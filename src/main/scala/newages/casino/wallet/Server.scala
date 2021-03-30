package newages.casino.wallet

import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.config.ConfigFactory
import newages.casino.wallet.controller.WalletController
import newages.casino.wallet.http.RouteBuilder
import newages.casino.wallet.service.account.{AccountPersistence, AccountService}
import newages.casino.wallet.service.{DoobiePersistence, GeneratorService}
import newages.casino.wallet.service.user.{UserPersistence, UserService}
import newages.casino.wallet.service.wallet.{WalletPersistence, WalletService}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object Server extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      generator <- GeneratorService.makeRef

      cf <- IO.delay(ConfigFactory.defaultApplication())
      dbContext <- IO.delay(
        ExecutionContext.fromExecutor(Executors.newFixedThreadPool(cf.getInt("doobie.threads")))
      )

      httpContext <- IO.delay(
        ExecutionContext.fromExecutor(Executors.newFixedThreadPool(cf.getInt("http.threads")))
      )

      db = DoobiePersistence(
        cf.getString("doobie.url"),
        cf.getString("doobie.user"),
        cf.getString("doobie.pass")
      )(dbContext)

      accountPersistence = AccountPersistence(db)
      walletPersistence = WalletPersistence(db)
      userPersistence = UserPersistence(db)

      accountService = AccountService(generator, accountPersistence)
      walletService = WalletService(generator, accountService, walletPersistence)
      userService = UserService(walletService, userPersistence)

      walletController = WalletController(userService, accountService)
      routers = RouteBuilder.makeRoute(walletController)

      result <- BlazeServerBuilder[IO](httpContext)
        .bindHttp(8080, "localhost")
        .withHttpApp(routers.orNotFound)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)

    } yield result

  def startPersistenceInDocker(): Unit = {}
}
