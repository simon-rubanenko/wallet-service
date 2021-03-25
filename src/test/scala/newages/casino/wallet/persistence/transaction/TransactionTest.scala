package newages.casino.wallet.persistence.transaction

import akka.Done
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, ActorTestKitBase}
import akka.pattern.StatusReply
import com.typesafe.config.ConfigFactory
import newages.casino.wallet.model.{AccountId, Money, Payment, PaymentTypeId, TransactionId}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.util.UUID

class TransactionTest extends AnyFunSuite with Matchers {

  val testKit: ActorTestKit = ActorTestKit(
    ActorTestKitBase.testNameFromCallStack(),
    ConfigFactory.parseString(s"""
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
      akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
    """)
  )

  private val counter = new java.util.concurrent.atomic.AtomicInteger(0)
  def newTransactionId: TransactionId = TransactionId(s"transaction-${counter.getAndIncrement()}")

  test("The transaction should be opened") {
    val transactionId = newTransactionId
    val payment = Payment(AccountId("1"), AccountId("2"), Money(10), PaymentTypeId("buy goods"))
    val transaction = testKit.spawn(TransactionEntity(transactionId))
    val probe = testKit.createTestProbe[StatusReply[Done]]()
    transaction ! TransactionCommands.MakePayments(Set(payment), probe.ref)
    probe.expectMessage(StatusReply.Success(Done))
  }

  test("should opened and complete transaction") {
    val transactionId = newTransactionId
    val payment = Payment(AccountId("1"), AccountId("2"), Money(10), PaymentTypeId("buy goods"))
    val transaction = testKit.spawn(TransactionEntity(transactionId))
    val probe = testKit.createTestProbe[StatusReply[Done]]()
    transaction ! TransactionCommands.MakePayments(Set(payment), probe.ref)
    probe.expectMessage(StatusReply.Success(Done))
    transaction ! TransactionCommands.TransactionCompleted(probe.ref)
    probe.expectMessage(StatusReply.Success(Done))
  }

}
