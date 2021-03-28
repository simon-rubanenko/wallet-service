package newages.casino.wallet.utils

import com.github.dockerjava.api.model.ExposedPort

import scala.concurrent.Future
import scala.concurrent.duration.{DurationInt, FiniteDuration}

trait DockerPostgreService extends DockerKit {

  val dockerContainer: DockerContainer = DockerContainer(image = "postgres:11")
    .withDockerHost("tcp://localhost:2375")
    .withPortsMapping(Map(ExposedPort.tcp(5432) -> 5432))
    .withEnv(Set("POSTGRES_PASSWORD=suitup", "POSTGRES_USER=nph"))
    .withName("Postgre11")

  private class PostgreChecker extends DockerReadyChecker {
    override val attempt: Int = 5
    override val delay: FiniteDuration = 5.seconds

    override def check: Future[Boolean] = {}
  }

}
