package newages.casino.wallet.service

import cats.Functor
import cats.effect.{IO, Sync}
import cats.effect.concurrent.Ref

trait GeneratorService[F[_], A] {
  def nextId: F[A]
}

class SimpleIncrementalGeneratorService[F[_]: Functor](value: Ref[F, Long])
    extends GeneratorService[F, String] {
  override def nextId: F[String] =
    Functor[F].map(value.getAndUpdate(_ + 1))(_.toString)
}

object SimpleIncrementalGeneratorService {
  def makeRef: IO[GeneratorService[IO, String]] =
    Ref.of(0L)(Sync[IO]).map(new SimpleIncrementalGeneratorService(_))
}
