package newages.casino.wallet.service.account

import cats.effect.IO
import com.github.dockerjava.api.model.{ExposedPort, Frame, HostConfig, Ports}
import com.github.dockerjava.core.DefaultDockerClientConfig
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import doobie._
import doobie.postgres.implicits._
import doobie.implicits._
import doobie.util.ExecutionContexts
import doobie.implicits._
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import com.github.dockerjava.api.async.ResultCallback
import newages.casino.wallet.utils.DockerPostgreService

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

class AccountPersistenceTest
    extends AnyFunSuite
    with Matchers
    with DockerPostgreService
    with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startContainer()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    stopContainer()
  }

  implicit val cs = IO.contextShift(ExecutionContext.global)

  case class Player(player_id: String)

  test("should connect to postgre db in docker") {
    ///
    val xa = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/postgres",
      "nph",
      "suitup"
    )

    val result = (for {
      _ <- sql"CREATE DATABASE test1".update.run
      _ <- sql"create table test1.player(player_id VARCHAR(255))".update.run
      c <- sql"insert into test1.player values('player id 1')".update.run
      res <- sql"select * from test1.player".query[Player].option
    } yield res)
      .transact(xa)
      .attempt
      .map {
        case Left(e) => println(s"E = $e")
      }
      .unsafeRunSync()

    println(s"RESULT = $result")
  }

}
