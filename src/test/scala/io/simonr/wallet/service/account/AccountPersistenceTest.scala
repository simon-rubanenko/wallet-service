package io.simonr.wallet.service.account

import cats.effect.IO
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import doobie._
import doobie.implicits._
import cats.effect._
import io.simonr.utils.doobie.DoobiePersistence
import io.simonr.utils.docker.DockerPostgreService
import io.simonr.wallet.model.{AccountId, Amount}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.Try

class AccountPersistenceTest extends AnyFunSuite with Matchers with BeforeAndAfterAll {

  val docker: DockerPostgreService =
    DockerPostgreService("postgreuser", "defaultpassword", "walletdb")

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val db: DoobiePersistence = docker.makeDoobiePersistence

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    docker.smartStartPostgreDocker()
    createAccountSchema("/service/account/schema.sql")
      .transact(db.autoCommitTransactor)
      .unsafeRunSync()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    docker.smartStopPostgreDocker()
  }

  def createAccountSchema(schemaPath: String): doobie.ConnectionIO[Int] = {
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

  test("should make deposit on account") {
    val accountPersistence = AccountPersistence(db)
    val accountId = AccountId("acc1")
    val (balance1, balance2) = (for {
      _ <- accountPersistence.addAccount(accountId)
      balance1 <- accountPersistence.deposit(accountId, Amount(123.45))
      balance2 <- accountPersistence.getBalance(accountId)
    } yield (balance1, balance2))
      .unsafeRunSync()

    balance1 shouldEqual balance2
    balance2 shouldEqual Amount(123.45)
  }

  test("should make withdraw from account") {
    val accountPersistence = AccountPersistence(db)
    val accountId = AccountId("acc2")
    val (balance1, balance2, balance3) = (for {
      _ <- accountPersistence.addAccount(accountId)
      balance1 <- accountPersistence.deposit(accountId, Amount(123.45))
      balance2 <- accountPersistence.withdraw(accountId, Amount(100.00))
      balance3 <- accountPersistence.getBalance(accountId)
    } yield (balance1, balance2, balance3))
      .unsafeRunSync()

    balance1 shouldEqual Amount(123.45)
    balance2 shouldEqual balance3
    balance3 shouldEqual Amount(23.45)
  }

  test("should catch throw Insufficient funds") {
    val accountPersistence = AccountPersistence(db)
    val accountId = AccountId("acc2")
    assertThrows[Throwable] {
      (for {
        _ <- accountPersistence.addAccount(accountId)
        _ <- accountPersistence.deposit(accountId, Amount(123.45))
        _ <- accountPersistence.withdraw(accountId, Amount(200.00))
      } yield ())
        .unsafeRunSync()
    }
  }

}
