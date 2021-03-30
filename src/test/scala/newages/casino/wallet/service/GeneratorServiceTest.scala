package newages.casino.wallet.service

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GeneratorServiceTest extends AnyFunSuite with Matchers {

  test("should return seq") {
    val result = for {
      gen <- GeneratorService.makeRef
      v1 <- gen.nextId
      v2 <- gen.nextId
    } yield (v1, v2)

    result.unsafeRunSync() shouldEqual ("1", "2")
  }
}
