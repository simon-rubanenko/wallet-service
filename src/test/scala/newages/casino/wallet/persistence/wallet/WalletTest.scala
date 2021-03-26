package newages.casino.wallet.persistence.wallet

import akka.Done
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, ActorTestKitBase}
import akka.pattern.StatusReply
import com.typesafe.config.ConfigFactory
import newages.casino.wallet.model.{AccountId, ActionResult, Currency, WalletId}
import newages.casino.wallet.persistence.wallet.WalletCommands.WalletDetails
import newages.casino.wallet.persistence.wallet.WalletStates.AccountInfo
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class WalletTest extends AnyFunSuite with Matchers {

  val testKit: ActorTestKit = ActorTestKit(
    ActorTestKitBase.testNameFromCallStack(),
    ConfigFactory.parseString(s"""
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
      akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
    """)
  )

  private val counter = new java.util.concurrent.atomic.AtomicInteger(0)
  def newWalletId: WalletId = WalletId(s"wallet-${counter.getAndIncrement()}")

  test("The wallet should be created") {
    val walletId = newWalletId
    val wallet = testKit.spawn(WalletEntity(walletId))
    val probe = testKit.createTestProbe[ActionResult[Done]]()
    wallet ! WalletCommands.CreateWallet(probe.ref)
    probe.expectMessage(ActionResult.done)
  }

  test("should created account and added it to the wallet") {
    val walletId = newWalletId
    val accountId = AccountId("1")
    val currency = Currency.default
    val wallet = testKit.spawn(WalletEntity(walletId))
    val probe = testKit.createTestProbe[ActionResult[Done]]()
    val probe2 = testKit.createTestProbe[ActionResult[WalletDetails]]()
    wallet ! WalletCommands.CreateWallet(probe.ref)
    probe.expectMessage(ActionResult.done)

    wallet ! WalletCommands.AddAccount(accountId, currency.id, probe2.ref)
    probe2.expectMessage(
      ActionResult.success(WalletDetails(Map(accountId -> AccountInfo(currency.id))))
    )
  }

}
