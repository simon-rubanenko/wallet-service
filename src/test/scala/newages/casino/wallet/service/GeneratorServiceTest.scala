package newages.casino.wallet.service

import cats.effect.{IO, Sync}
import cats.effect.concurrent.Ref
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GeneratorServiceTest extends AnyFunSuite with Matchers {

  test("should return seq") {
    val result = for {
      gen <- SimpleIncrementalGeneratorService.makeRef
      v1 <- gen.nextId
      v2 <- gen.nextId
    } yield (v1, v2)

    result.unsafeRunSync() shouldEqual ("1", "2")
  }

//  test("should return seq 2") {
//    val gen = Ref.of(0L)(Sync[IO]).map(new SimpleIncrementalGeneratorService(_))
//    val res = (for {
//      v1 <- gen.flatMap(_.nextId)
//      v2 <- gen.flatMap(_.nextId)
//    } yield (v1, v2))
//      .unsafeRunSync()
//
//    res shouldEqual ("1", "2")
//  }

}
