package newages.casino.wallet.service.user

import cats.effect.{IO, _}
import doobie._
import doobie.implicits._
import io.simonr.utils.docker.DockerPostgreService
import io.simonr.utils.doobie.DoobiePersistence
import newages.casino.wallet.model.{UserId, WalletId}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.Try

class UserPersistenceTest
    extends AnyFunSuite
    with Matchers
    with DockerPostgreService
    with BeforeAndAfterAll {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val db: DoobiePersistence = makeDoobiePersistence

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startContainer()
    createWalletSchema("/service/user/schema.sql")
      .transact(db.autoCommitTransactor)
      .unsafeRunSync()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    stopContainer()
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

  test("should add user") {
    val userPersistence = UserPersistence(db)
    val userId = UserId("user#1")
    val walletId = WalletId("wallet#1")
    userPersistence.addUser(userId, walletId)
      .unsafeRunSync() shouldEqual ()

    sql"""select user_id from wallet_user.user where user_id = ${userId.id}"""
      .query[UserId]
      .option
      .transact(db.autoCommitTransactor)
      .unsafeRunSync() shouldEqual Some(userId)
  }

  test("should return user wallet id") {
    val userPersistence = UserPersistence(db)
    val userId = UserId("user#2")
    val walletId = WalletId("wallet#2")
    (for {
      _ <- userPersistence.addUser(userId, walletId)
      walletId <- userPersistence.getUserWalletId(userId)
    } yield walletId)
      .unsafeRunSync() shouldEqual Some(walletId)
  }

}
