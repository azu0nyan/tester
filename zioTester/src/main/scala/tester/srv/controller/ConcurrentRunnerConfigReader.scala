package tester.srv.controller

import com.github.dockerjava.core.{DefaultDockerClientConfig, DockerClientConfig}
import main.Configs
import zio.{ZIO, ZLayer}
import zioDockerRunner.testRunner.ConcurrentRunner.ConcurrentRunnerConfig

object ConcurrentRunnerConfigReader {

  import pureconfig.*
  import pureconfig.generic.semiauto.*

  case class DockerClientConfigFromFile(
                                         dockerHost: Option[String],
                                         dockerTslVerify: Option[Boolean],
                                         dockerCertPath: Option[String],
                                         dockerConfig : Option[String],
                                         apiVersion : Option[String],
                                         registryUrl: Option[String],
                                         registryUsername: Option[String],
                                         registryPassword: Option[String],
                                         registryEmail: Option[String],
                                       ){
    def toDockerClientConfig: DockerClientConfig = {
      val conf = DefaultDockerClientConfig
        .createDefaultConfigBuilder()

      dockerHost.fold(conf)(h => conf.withDockerHost(h))
      dockerTslVerify.fold(conf)(v => conf.withDockerTlsVerify(v))
      dockerCertPath.fold(conf)(p => conf.withDockerCertPath(p))
      dockerConfig.fold(conf)(c => conf.withDockerConfig(c))
      apiVersion.fold(conf)(v => conf.withApiVersion(v))
      registryUrl.fold(conf)(u => conf.withRegistryUrl(u))
      registryUsername.fold(conf)(u => conf.withRegistryUsername(u))
      registryPassword.fold(conf)(p => conf.withRegistryPassword(p))
      registryEmail.fold(conf)(e => conf.withRegistryEmail(e))

      conf.build()
    }
  }
  case class ConcurrentRunnerConfigFromFile(
                                     fibersMax: Int,
                                     containerName: String,
                                     dockerClientConfig: DockerClientConfigFromFile,
                                     runnerName: String = "Default runner"
                                   ){
    def toConcurrentRunnerConfig: ConcurrentRunnerConfig = ConcurrentRunnerConfig(
      fibersMax,
      containerName,
      dockerClientConfig.toDockerClientConfig,
      runnerName
    )
  }

  case class RunnersConfigFromFile(
                                    queueSize: Int,
                                    runners: Seq[ConcurrentRunnerConfigFromFile],
                                   )

  given ConfigReader[ConcurrentRunnerConfigFromFile] = deriveReader
  given ConfigReader[RunnersConfigFromFile] = deriveReader

  val config = ZIO.succeed {
    val config: ConfigReader.Result[RunnersConfigFromFile] =
      Configs.config.at("runners").load[RunnersConfigFromFile]
    if (config.isLeft) println(config) //todo log better
    config.right.get
  }
}
