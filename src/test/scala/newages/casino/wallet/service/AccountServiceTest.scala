package newages.casino.wallet.service

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, ActorTestKitBase}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import newages.casino.wallet.model.{AccountId, ActionResult}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.util.UUID
import scala.concurrent.duration.DurationInt

class AccountServiceTest extends AnyFunSuite with Matchers with ScalaFutures {

  val testKit: ActorTestKit = ActorTestKit(
    ActorTestKitBase.testNameFromCallStack(),
    ConfigFactory.parseString(s"""
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
      akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
    """)
  )

  implicit val system = testKit.system
  implicit val timeout: Timeout = 5.seconds

  test("should create account") {
    val accountId = AccountId("1")
    val service = new AccountServiceImpl()
    whenReady(service.createAccount(accountId)) { result =>
      result shouldEqual ActionResult.done
    }

  }
}
