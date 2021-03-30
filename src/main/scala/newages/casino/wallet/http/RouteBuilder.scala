package newages.casino.wallet.http

import cats.data.Kleisli
import cats.effect.IO
import fs2.Stream
import fs2.text.utf8Encode
import io.circe.Encoder
import io.circe.generic.auto.exportEncoder
import io.circe.generic.semiauto.deriveEncoder
import newages.casino.wallet.controller
import newages.casino.wallet.controller.{Balance, JsonBase, WalletController}
import org.http4s.{EntityBody, HttpRoutes, Request, Response, _}
import org.http4s.implicits._
import org.http4s.dsl.io._
import io.circe.syntax._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.impl.PathVar

import scala.util.Try

object RouteBuilder extends controller.JsonEncoders {

  def makeRoute(walletController: WalletController): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case PUT -> Root / "wallet" / "register" / playerId =>
        walletController
          .registerPlayer(playerId)
          .map(validateResult)

      case POST -> Root / "wallet" / "deposit" / playerId / BigDecimalVar(amount) =>
        walletController
          .deposit(playerId, amount)
          .map(validateResult)

      case POST -> Root / "wallet" / "withdraw" / playerId / BigDecimalVar(amount) =>
        walletController
          .withdraw(playerId, amount)
          .map(validateResult)

      case GET -> Root / "balance" / playerId =>
        Ok(s"Balance, $playerId")

    }

  private def validateResult(result: Either[controller.Error, Balance]): Response[IO] =
    result match {
      case Right(v) =>
        Response(status = Ok).withEntity(v.asJson)
      case Left(e) => Response(status = Status.BadRequest).withEntity(e.asJson)

    }

  private object BigDecimalVar {
    def unapply(str: String): Option[BigDecimal] =
      if (str.nonEmpty)
        Try(BigDecimal(str)).toOption
      else
        None
  }
}
