package io.simonr.wallet

import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.config.ConfigFactory
import io.simonr.utils.doobie.DoobiePersistence
import io.simonr.wallet.http.RouteBuilder
import io.simonr.wallet.service.GeneratorService
import io.simonr.wallet.service.account.{AccountPersistence, AccountService}
import io.simonr.wallet.service.user.{UserPersistence, UserService}
import io.simonr.wallet.service.wallet.{WalletPersistence, WalletService}
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

      /// TODO implement db evolution
      _ <- TestLoader.start(db)

      accountPersistence = AccountPersistence(db)
      walletPersistence = WalletPersistence(db)
      userPersistence = UserPersistence(db)

      accountService = AccountService(generator, accountPersistence)
      walletService = WalletService(generator, accountService, walletPersistence)
      userService = UserService(walletService, userPersistence)

      walletController = controller.WalletController(userService, accountService)
      routers = RouteBuilder.makeRoute(walletController)

      result <- BlazeServerBuilder[IO](httpContext)
        .bindHttp(8080, "localhost")
        .withHttpApp(routers.orNotFound)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)

    } yield result

}

object TestLoader {
  import scala.util.Try
  import scala.io.Source
  import doobie._
  import doobie.implicits._
  import cats.effect._

  def start(db: DoobiePersistence): IO[Unit] =
    (for {
      _ <- loadSchema("/service/account/schema.sql")
      _ <- loadSchema("/service/user/schema.sql")
      _ <- loadSchema("/service/wallet/schema.sql")
    } yield ())
      .transact(db.autoCommitTransactor)

  def loadSchema(schemaPath: String): doobie.ConnectionIO[Int] = {
    val schema = Try(Source.fromInputStream(this.getClass.getResourceAsStream(schemaPath)).mkString)
      .getOrElse(
        throw new Exception(
          "can't read file: " + schemaPath + "\n probably need to add resource to BUILD file"
        )
      )

    Fragment.const(schema)
      .update
      .run
  }
}
