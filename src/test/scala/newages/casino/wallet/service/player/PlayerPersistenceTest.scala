package newages.casino.wallet.service.player

import cats.effect.{IO, _}
import doobie._
import doobie.implicits._
import newages.casino.wallet.model.{PlayerId, WalletId}
import newages.casino.wallet.service.DoobiePersistence
import newages.casino.wallet.utils.DockerPostgreService
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.Try

class PlayerPersistenceTest
    extends AnyFunSuite
    with Matchers
    with DockerPostgreService
    with BeforeAndAfterAll {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val db: DoobiePersistence = makeDoobiePersistence

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startContainer()
    createWalletSchema("/service/player/schema.sql")
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

  test("should add player") {
    val playerPersistence = PlayerPersistence(db)
    val playerId = PlayerId("player#1")
    val walletId = WalletId("wallet#1")
    playerPersistence.addPlayer(playerId, walletId)
      .unsafeRunSync() shouldEqual ()

    sql"""select player_id from player.player where player_id = ${playerId.id}"""
      .query[PlayerId]
      .option
      .transact(db.autoCommitTransactor)
      .unsafeRunSync() shouldEqual Some(playerId)
  }

  test("should return player wallet id") {
    val playerPersistence = PlayerPersistence(db)
    val playerId = PlayerId("player#2")
    val walletId = WalletId("wallet#2")
    (for {
      _ <- playerPersistence.addPlayer(playerId, walletId)
      walletId <- playerPersistence.getPlayerWalletId(playerId)
    } yield walletId)
      .unsafeRunSync() shouldEqual Some(walletId)
  }

}
