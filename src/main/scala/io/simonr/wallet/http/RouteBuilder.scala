package io.simonr.wallet.http

import cats.effect.IO
import org.http4s.{HttpRoutes, Response, _}
import org.http4s.dsl.io._
import io.circe.syntax._
import io.simonr.wallet.controller
import io.simonr.wallet.controller.{Balance, WalletController}
import org.http4s.circe.CirceEntityEncoder._

import scala.util.Try

object RouteBuilder extends controller.JsonEncoders {

  def makeRoute(walletController: WalletController): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case PUT -> Root / "wallet" / "register" / playerId =>
        walletController
          .registerUser(playerId)
          .map(validateResult)

      case POST -> Root / "wallet" / "deposit" / playerId / BigDecimalVar(amount) =>
        walletController
          .deposit(playerId, amount)
          .map(validateResult)

      case POST -> Root / "wallet" / "withdraw" / playerId / BigDecimalVar(amount) =>
        walletController
          .withdraw(playerId, amount)
          .map(validateResult)

      case GET -> Root / "wallet" / "balance" / playerId =>
        walletController
          .getBalance(playerId)
          .map(validateResult)
    }

  private def validateResult(result: Either[controller.Error, Balance]): Response[IO] =
    result match {
      case Right(v) => Response(status = Ok).withEntity(v.asJson)

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
