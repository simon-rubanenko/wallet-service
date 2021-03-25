package newages.casino.wallet.persistence.account

import akka.Done
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, ActorTestKitBase}
import akka.pattern.StatusReply
import com.typesafe.config.ConfigFactory
import newages.casino.wallet.model.{AccountId, Money}
import newages.casino.wallet.persistence.account.AccountCommands.AccountBalance
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class AccountTest extends AnyFunSuite with Matchers {

  val testKit: ActorTestKit = ActorTestKit(
    ActorTestKitBase.testNameFromCallStack(),
    ConfigFactory.parseString(s"""
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
      akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
    """)
  )

  private val counter = new java.util.concurrent.atomic.AtomicInteger(0)
  def newAccountId: AccountId = AccountId(s"account-${counter.getAndIncrement()}")

  test("Account should be created") {
    val accountId = newAccountId
    val account = testKit.spawn(AccountEntity(accountId))
    val probe = testKit.createTestProbe[StatusReply[Done]]()
    account ! AccountCommands.CreateAccount(probe.ref)
    probe.expectMessage(StatusReply.Success(Done))
  }

  test("should make deposit and get balance") {
    val accountId = newAccountId
    val account = testKit.spawn(AccountEntity(accountId))
    val probe1 = testKit.createTestProbe[StatusReply[Done]]()
    val probe2 = testKit.createTestProbe[StatusReply[AccountBalance]]()
    account ! AccountCommands.CreateAccount(probe1.ref)
    probe1.expectMessage(StatusReply.Success(Done))
    account ! AccountCommands.Deposit(Money(10.0), probe2.ref)
    probe2.expectMessage(StatusReply.Success(AccountBalance(Money(10.0))))
  }

  test("should make withdraw and get balance") {
    val accountId = newAccountId
    val account = testKit.spawn(AccountEntity(accountId))
    val probe1 = testKit.createTestProbe[StatusReply[Done]]()
    val probe2 = testKit.createTestProbe[StatusReply[AccountBalance]]()
    account ! AccountCommands.CreateAccount(probe1.ref)
    probe1.expectMessage(StatusReply.Success(Done))
    account ! AccountCommands.Deposit(Money(10.0), probe2.ref)
    probe2.expectMessage(StatusReply.Success(AccountBalance(Money(10.0))))

    account ! AccountCommands.Withdraw(Money(4.0), probe2.ref)
    probe2.expectMessage(StatusReply.Success(AccountBalance(Money(6.0))))
  }

  test("should get error on attempt to withdraw & make negative balance") {
    val accountId = newAccountId
    val account = testKit.spawn(AccountEntity(accountId))
    val probe1 = testKit.createTestProbe[StatusReply[Done]]()
    val probe2 = testKit.createTestProbe[StatusReply[AccountBalance]]()
    account ! AccountCommands.CreateAccount(probe1.ref)
    probe1.expectMessage(StatusReply.Success(Done))
    account ! AccountCommands.Deposit(Money(10.0), probe2.ref)
    probe2.expectMessage(StatusReply.Success(AccountBalance(Money(10.0))))

    account ! AccountCommands.Withdraw(Money(15.0), probe2.ref)
    probe2.expectMessage(AccountCommands.replyInsufficientBalance(Money(10.0), Money(15.0)))
  }

}
