package io.simonr.wallet.service.wallet

import cats.effect.{IO, _}
import doobie._
import doobie.implicits._
import io.simonr.utils.docker.DockerPostgreService
import io.simonr.utils.doobie.DoobiePersistence
import io.simonr.wallet.model.{AccountId, Currency, CurrencyId, WalletId}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.Try

class WalletPersistenceTest extends AnyFunSuite with Matchers with BeforeAndAfterAll {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val docker: DockerPostgreService =
    DockerPostgreService("postgreuser", "defaultpassword", "walletdb")

  val db: DoobiePersistence = docker.makeDoobiePersistence

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    docker.smartStartPostgreDocker()
    createWalletSchema("/service/wallet/schema.sql")
      .transact(db.autoCommitTransactor)
      .unsafeRunSync()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    docker.smartStopPostgreDocker()
  }

  def createWalletSchema(schemaPath: String): doobie.ConnectionIO[Int] = {
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

  test("should add wallet") {
    val walletPersistence = WalletPersistence(db)
    val walletId = WalletId("wallet#1")
    walletPersistence.addWallet(walletId)
      .unsafeRunSync() shouldEqual ()

    sql"""select wallet_id from wallet.wallet where wallet_id = ${walletId.id}"""
      .query[WalletId]
      .unique
      .transact(db.autoCommitTransactor)
      .unsafeRunSync() shouldEqual walletId
  }

  test("should add account") {
    val walletPersistence = WalletPersistence(db)
    val walletId = WalletId("wallet#1")
    val accountId = AccountId("acc1")
    val currency = Currency.default
    walletPersistence
      .addAccount(walletId, accountId, currency.id)
      .unsafeRunSync() shouldEqual ()

    val selectRes =
      sql"""select wallet_account_wallet_id, wallet_account_account_id, wallet_account_currency_id
         from wallet.wallet_account
         where wallet_account_wallet_id = ${walletId.id}
          and wallet_account_account_id = ${accountId.id}"""
        .query[(WalletId, AccountId, CurrencyId)]
        .stream
        .take(5)
        .compile
        .toList
        .transact(db.autoCommitTransactor)
        .unsafeRunSync()

    selectRes should have length 1
    selectRes.head shouldEqual (walletId, accountId, currency.id)
  }

  test("should return account by currency id") {
    val walletPersistence = WalletPersistence(db)
    val walletId = WalletId("wallet#2")
    val accountId = AccountId("acc2")
    val currency = Currency.default
    val (acc1, acc2) = (for {
      _ <- walletPersistence.addWallet(walletId)
      _ <- walletPersistence.addAccount(walletId, accountId, currency.id)
      acc1 <- walletPersistence.getAccountByCurrency(walletId, currency.id)
      acc2 <- walletPersistence.getAccountByCurrency(walletId, CurrencyId("unknown id"))
    } yield (acc1, acc2))
      .unsafeRunSync()

    acc1 shouldEqual Some(accountId)
    acc2 shouldEqual None
  }

}
