package newages.casino.wallet.service.account

import cats.effect.IO
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import doobie._
import doobie.implicits._
import cats.effect._
import cats.implicits.catsSyntaxEitherId
import newages.casino.wallet.model.{AccountId, Amount}
import newages.casino.wallet.persistence.DoobiePersistence
import newages.casino.wallet.utils.DockerPostgreService

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.Try

class AccountPersistenceTest
    extends AnyFunSuite
    with Matchers
    with DockerPostgreService
    with BeforeAndAfterAll {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val db: DoobiePersistence = makeDoobiePersistence

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startContainer()
    createAccountSchema("/service/account/schema.sql")
      .transact(db.autoCommitTransactor)
      .unsafeRunSync()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    stopContainer()
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
    val accountId = AccountId("acc#1")
    val (balance1, balance2) = (for {
      _ <- accountPersistence.addAccount(accountId)
      balance1 <- accountPersistence.deposit(accountId, Amount(123.45))
      balance2 <- accountPersistence.getBalance(accountId)
    } yield (balance1, balance2))
      .unsafeRunSync()

    balance1 shouldEqual balance2
    balance2 shouldEqual Amount(123.45).asRight
  }

  test("should make withdraw from account") {
    val accountPersistence = AccountPersistence(db)
    val accountId = AccountId("acc#2")
    val (balance1, balance2, balance3) = (for {
      _ <- accountPersistence.addAccount(accountId)
      balance1 <- accountPersistence.deposit(accountId, Amount(123.45))
      balance2 <- accountPersistence.withdraw(accountId, Amount(100.00))
      balance3 <- accountPersistence.getBalance(accountId)
    } yield (balance1, balance2, balance3))
      .unsafeRunSync()

    balance1 shouldEqual Amount(123.45).asRight
    balance2 shouldEqual balance3
    balance3 shouldEqual Amount(23.45).asRight
  }

}
