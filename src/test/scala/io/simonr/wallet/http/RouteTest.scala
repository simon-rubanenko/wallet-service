package io.simonr.wallet.http

import cats.effect.IO
import io.circe.{Decoder, Json}
import io.circe.generic.semiauto.deriveDecoder
import io.circe.syntax.EncoderOps
import io.simonr.wallet.controller
import io.simonr.wallet.controller.{Balance, Error, WalletController}
import org.http4s.{Request, _}
import org.http4s.implicits._
import org.http4s.dsl.io._
import org.mockito.cats.MockitoCats.whenF
import org.mockito.scalatest.{MockitoSugar, ResetMocksAfterEachTest}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.http4s.circe.CirceEntityDecoder._

class RouteTest
    extends AnyFunSuite
    with Matchers
    with MockitoSugar
    with ResetMocksAfterEachTest
    with controller.JsonEncoders {

  val controllerMock: WalletController = mock[WalletController]

  implicit lazy val balanceDecoder: Decoder[Balance] = deriveDecoder[Balance]

  test("should register player and return it balance") {
    val balance = Balance(0.0)
    whenF(controllerMock.registerUser(any)).thenReturn(Right(balance))

    val service = RouteBuilder.makeRoute(controllerMock)
    val request = Request[IO](PUT, uri"/wallet/register/player1")
    val response = service
      .orNotFound(request)
      .unsafeRunSync()
    response.status shouldEqual Status.Ok
    response.as[Json].unsafeRunSync() shouldEqual balance.asJson
  }

  test("should got error player already registered by register") {
    val message = Error("player already registered")
    whenF(controllerMock.registerUser(any)).thenReturn(Left(message))

    val service = RouteBuilder.makeRoute(controllerMock)
    val request = Request[IO](PUT, uri"/wallet/register/player1")
    val response = service
      .orNotFound(request)
      .unsafeRunSync()
    response.status shouldEqual Status.BadRequest
    response.as[Json].unsafeRunSync() shouldEqual message.asJson
  }

  test("should make deposit") {
    val amount = BigDecimal(12.34)
    val balance = Balance(amount)
    whenF(controllerMock.deposit(any, any)).thenReturn(Right(balance))

    val service = RouteBuilder.makeRoute(controllerMock)
    val request = Request[IO](POST, Uri.unsafeFromString(s"/wallet/deposit/player1/$amount"))
    val response = service
      .orNotFound(request)
      .unsafeRunSync()
    response.status shouldEqual Status.Ok
    response.as[Json].unsafeRunSync() shouldEqual balance.asJson
  }

  test("should got error message on make deposit") {
    val message = Error("player not found")
    whenF(controllerMock.deposit(any, any)).thenReturn(Left(message))

    val service = RouteBuilder.makeRoute(controllerMock)
    val request = Request[IO](POST, uri"/wallet/deposit/player1/12.34")
    val response = service
      .orNotFound(request)
      .unsafeRunSync()
    response.status shouldEqual Status.BadRequest
    response.as[Json].unsafeRunSync() shouldEqual message.asJson
  }

  test("should make withdraw") {
    val amount = BigDecimal(12.34)
    val balance = Balance(amount)
    whenF(controllerMock.withdraw(any, any)).thenReturn(Right(balance))

    val service = RouteBuilder.makeRoute(controllerMock)
    val request = Request[IO](POST, Uri.unsafeFromString(s"/wallet/withdraw/player1/$amount"))
    val response = service
      .orNotFound(request)
      .unsafeRunSync()
    response.status shouldEqual Status.Ok
    response.as[Json].unsafeRunSync() shouldEqual balance.asJson
  }

  test("should got error message on make withdraw") {
    val message = Error("player not found")
    whenF(controllerMock.withdraw(any, any)).thenReturn(Left(message))

    val service = RouteBuilder.makeRoute(controllerMock)
    val request = Request[IO](POST, uri"/wallet/withdraw/player1/12.34")
    val response = service
      .orNotFound(request)
      .unsafeRunSync()
    response.status shouldEqual Status.BadRequest
    response.as[Json].unsafeRunSync() shouldEqual message.asJson
  }

  test("should get balance") {
    val amount = BigDecimal(12.34)
    val balance = Balance(amount)
    whenF(controllerMock.getBalance(any)).thenReturn(Right(balance))

    val service = RouteBuilder.makeRoute(controllerMock)
    val request = Request[IO](GET, Uri.unsafeFromString(s"/wallet/balance/player1"))
    val response = service
      .orNotFound(request)
      .unsafeRunSync()
    response.status shouldEqual Status.Ok
    response.as[Json].unsafeRunSync() shouldEqual balance.asJson
  }

  test("should got error message on get balance") {
    val message = Error("player not found")
    whenF(controllerMock.getBalance(any)).thenReturn(Left(message))

    val service = RouteBuilder.makeRoute(controllerMock)
    val request = Request[IO](GET, uri"/wallet/balance/player1")
    val response = service
      .orNotFound(request)
      .unsafeRunSync()
    response.status shouldEqual Status.BadRequest
    response.as[Json].unsafeRunSync() shouldEqual message.asJson
  }

}
