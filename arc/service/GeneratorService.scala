package newages.casino.wallet.service

import io.simonr.wallet.service

trait GeneratorService {
  def nextId: String
}

class SimpleRandomGeneratorService extends service.GeneratorService {
  override def nextId: String = java.util.UUID.randomUUID().toString
}

class SimpleIncrementalGeneratorService extends service.GeneratorService {
  val atomLong = new java.util.concurrent.atomic.AtomicLong(1)
  override def nextId: String = atomLong.getAndIncrement().toString
}
