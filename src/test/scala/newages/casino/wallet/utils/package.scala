package newages.casino.wallet

import com.github.dockerjava.api.model.{ExposedPort, InternetProtocol, PortBinding, Ports}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

package object utils {
  case class DockerContainer(
      image: String,
      name: Option[String] = None,
      env: Set[String] = Set.empty,
      portsMapping: Map[ExposedPort, Int] = Map.empty,
      dockerHost: Option[String] = None,
      readyChecker: Option[DockerReadyChecker] = None
  ) {
    def withName(name: String): DockerContainer = copy(name = Some(name))

    def withEnv(env: Set[String]): DockerContainer = copy(env = env)

    def withPortsMapping(portsMapping: Map[ExposedPort, Int]): DockerContainer =
      copy(portsMapping = portsMapping)

    def withDockerHost(host: String): DockerContainer = copy(dockerHost = Some(host))

    def withReadyChecker(readyChecker: DockerReadyChecker): DockerContainer =
      copy(readyChecker = Some(readyChecker))

    def getTcpPortsMapping: Map[Int, Int] =
      portsMapping.collect {
        case (from: ExposedPort, to) if from.getProtocol == InternetProtocol.TCP =>
          from.getPort -> to
      }

    def getPortBindings: Set[PortBinding] =
      portsMapping.collect {
        case (from: ExposedPort, to) if from.getProtocol == InternetProtocol.TCP =>
          new PortBinding(Ports.Binding.bindPort(to), from)
      }.toSet
  }

  trait DockerReadyChecker {
    val attempt: Int
    val delay: FiniteDuration
    def check(implicit ec: ExecutionContext): Future[Boolean]
  }
}
