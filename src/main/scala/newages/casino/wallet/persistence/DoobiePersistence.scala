package newages.casino.wallet.persistence

import cats.effect.{Blocker, ContextShift, IO}
import doobie.free.connection.setAutoCommit
import doobie.util.transactor.Strategy
import doobie.util.transactor.Transactor.Aux
import doobie.{ExecutionContexts, Transactor}

import scala.concurrent.ExecutionContext

trait DoobiePersistence {
  val autoCommitTransactor: Aux[IO, Unit]
  val defaultTransactor: Aux[IO, Unit]
}

object DoobiePersistence {
  def apply(url: String, user: String, pass: String)(implicit
      ec: ExecutionContext
  ): DoobiePersistence = new DoobiePersistenceImpl(url, user, pass)
}

class DoobiePersistenceImpl(val url: String, val user: String, val pass: String)(implicit
    ec: ExecutionContext
) extends DoobiePersistence {

  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  val autoCommitTransactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = url,
    user = user,
    pass = pass,
    Blocker.liftExecutionContext(ec)
  ).copy(Strategy.void.copy(before = setAutoCommit(true)))

  val defaultTransactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = url,
    user = user,
    pass = pass,
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )
}
