package newages.casino.wallet

import cats.effect.{ExitCode, IO, IOApp}
import newages.casino.wallet.controller.WalletController
import newages.casino.wallet.http.RouteBuilder
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

class HttpServer extends IOApp {

  val walletController = WalletController()
  val routers = RouteBuilder.makeRoute(walletController)

  val httpContext =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](httpContext)
      .bindHttp(8080, "localhost")
      .withHttpApp(routers.orNotFound)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
