package newages.casino.wallet.utils

import cats.effect.{ContextShift, IO}
import com.github.dockerjava.api.model.ExposedPort
import newages.casino.wallet.service.DoobiePersistence

import java.sql.DriverManager
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

trait DockerPostgreService extends DockerKit {
  val (dbUser, dbPass) = ("nph", "suitup")

  val dockerContainer: DockerContainer = DockerContainer(image = "postgres:11")
    .withDockerHost("tcp://localhost:2375")
    .withPortsMapping(Map(ExposedPort.tcp(5432) -> 5432))
    .withEnv(Set("POSTGRES_PASSWORD=suitup", "POSTGRES_USER=nph"))
    .withReadyChecker(new PostgreDockerReadyChecker(dbUser, dbPass))
    .withName("Postgre11")

  private class PostgreDockerReadyChecker(val user: String, val password: String)
      extends DockerReadyChecker {
    override val attempt: Int = 10
    override val delay: FiniteDuration = 3.seconds

    override def check(implicit ec: ExecutionContext): Future[Boolean] =
      Future {
        Class.forName("org.postgresql.Driver")
        Option(DriverManager.getConnection(url, user, password)).map(_.close).isDefined
      }
  }

  val url = s"jdbc:postgresql://localhost:${dockerContainer.getTcpPortsMapping.values.head}/"

  def makeDoobiePersistence(implicit ec: ExecutionContext): DoobiePersistence =
    DoobiePersistence(s"${url}postgres", dbUser, dbPass)
}
