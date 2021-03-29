package newages.casino.wallet.utils

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.model.{ExposedPort, HostConfig, Image, Ports}
import com.github.dockerjava.core.{DefaultDockerClientConfig, DockerClientImpl}
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient

import java.util
import java.util.{Timer, TimerTask}
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.jdk.CollectionConverters._

trait DockerKit {
  val dockerContainer: DockerContainer

  lazy val config: DefaultDockerClientConfig =
    DefaultDockerClientConfig.createDefaultConfigBuilder()
      .withDockerHost(dockerContainer.dockerHost.getOrElse("tcp://localhost:2375"))
      .withDockerTlsVerify(false)
      .build()

  lazy val httpClient: ApacheDockerHttpClient = new ApacheDockerHttpClient.Builder()
    .dockerHost(config.getDockerHost)
    .sslConfig(config.getSSLConfig).build

  lazy val dockerClient: DockerClient = DockerClientImpl.getInstance(config, httpClient)

  lazy val images: util.List[Image] = dockerClient.listImagesCmd().exec()

  lazy val cmd: CreateContainerResponse = dockerClient
    .createContainerCmd(dockerContainer.image)
    .withExposedPorts(dockerContainer.portsMapping.keySet.toList.asJava)
    .withHostConfig(new HostConfig().withPortBindings(
      dockerContainer.getPortBindings.toList.asJava
    ))
    .withEnv(dockerContainer.env.toList.asJava)
    .withName(dockerContainer.name.getOrElse(s"image name is [${dockerContainer.image}]"))
    .exec()

  def startContainer()(implicit ec: ExecutionContext): Unit = {
    dockerClient.startContainerCmd(cmd.getId)
      .exec()
    dockerContainer
      .readyChecker
      .map(v => waitForReady(v.check, v.attempt, v.delay))
      .foreach { f =>
        val r = Await.result(f, Duration.Inf)
      }
  }

  def stopContainer(): Unit = {
    dockerClient.stopContainerCmd(cmd.getId).exec()
    dockerClient.removeContainerCmd(cmd.getId).exec()
  }

  private def waitForReady[T](f: => Future[T], attempts: Int, delay: FiniteDuration)(implicit
      ec: ExecutionContext
  ): Future[T] = {
    def loop(rest: Int): Future[T] =
      f.recoverWith {
        case e => rest match {
            case 0 => Future.failed(e match {
                case _: NoSuchElementException =>
                  new NoSuchElementException(
                    s"Ready checker returned false after $attempts attempts, delayed $delay each"
                  )
                case _ => e
              })
            case n => withDelay(delay.toMillis)(loop(n - 1))
          }
      }

    loop(attempts)
  }

  private def withDelay[T](delay: Long)(f: => Future[T]): Future[T] = {
    val timer = new Timer()
    val promise = Promise[T]()
    timer.schedule(
      new TimerTask {
        override def run(): Unit = {
          promise.completeWith(f)
          timer.cancel()
        }
      },
      delay
    )
    promise.future
  }

}
