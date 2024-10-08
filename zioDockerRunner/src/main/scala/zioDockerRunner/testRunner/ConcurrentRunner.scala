package zioDockerRunner.testRunner

import com.github.dockerjava.core.{DefaultDockerClientConfig, DockerClientConfig}
import zio.{ZIO, *}
import zio.stream.ZStream
import zioDockerRunner.dockerIntegration.DockerOps
import zioDockerRunner.dockerIntegration.DockerOps.DockerFailure.{UnknownDockerFailure, WorkQueueIsFull}
import zioDockerRunner.dockerIntegration.DockerOps.{DockerClientContext, DockerFailure}
import zioDockerRunner.testRunner.ConcurrentRunner.*



case class ConcurrentRunner(queue: Queue[ConcurrentRunnerTask], configs: Seq[ConcurrentRunnerConfig]) {
  //  def addTaskExternal(crm: CompileAndRunMultiple): scala.concurrent.Future[DockerFailure | TaskResultPromise] =
  //    addTask(crm).toFuture.map(_.)

  def addTask(crm: CompileAndRunMultiple): UIO[TaskResultPromise] =
    for {
      prom <- Promise.make[DockerFailure, CompileAndRunMultipleResult]
      succ <- queue.offer(ConcurrentRunnerTask(crm, prom))
      _ <- ZIO.when(!succ)(prom.fail(WorkQueueIsFull))
      _ <- ZIO.logInfo(s"Adding task to queue $succ")
    } yield prom


  //todo mb ZIO.scoped here
  def runTask(taskAndPromise: ConcurrentRunnerTask, config: ConcurrentRunnerConfig): ZIO[Any, Nothing, Unit] =
    for {
      resExit: Exit[DockerFailure, CompileAndRunMultipleResult] <- Runner.compileAndRunMultiple(taskAndPromise.task)
        .provideSomeLayer(DockerOps.dockerClientContextScoped(config.containerName, config.dockerClientConfig)).exit
      _ <- resExit match
        case Exit.Success(value) => taskAndPromise.promise.succeed(value)
        case Exit.Failure(cause) => taskAndPromise.promise.fail(cause.failures.headOption.getOrElse(UnknownDockerFailure))
      } yield ()


  def fiberWorker(id: Int, config: ConcurrentRunnerConfig): ZIO[Any, Nothing, Unit] = {
    val workerLogString = s"FW $id for ${config.containerName} at ${config.dockerClientConfig.getDockerHost}"
    for {
      _ <- ZIO.logInfo(s"$workerLogString started.")
      _ <- (for {
        taskAndPromise <- queue.take
        _ <- ZIO.logInfo(s"$workerLogString begin running task.")
        _ <- runTask(taskAndPromise, config)
        _ <- ZIO.logInfo(s"$workerLogString end running task.")
      } yield ()).forever
      _ <- ZIO.logInfo(s"$workerLogString stopped")
    } yield ()
  }

  def startWorkers: ZIO[Any, Nothing, Unit] =
    ZIO.foreachDiscard(configs) { conf =>
      ZIO.foreachDiscard(0 until conf.fibersMax) { fId =>
        fiberWorker(fId, conf).fork
      }
    }


}

object ConcurrentRunner {
  
  case class ConcurrentRunnerConfig(
                                     fibersMax: Int,
                                     containerName: String,
                                     dockerClientConfig: DockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build(),
                                     runnerName: String = "Default runner"
                                   )

  type TaskResultPromise = Promise[DockerFailure, CompileAndRunMultipleResult]
  case class ConcurrentRunnerTask(task: CompileAndRunMultiple, promise: TaskResultPromise)
//  def taskFromCompileRunMultiple(task: CompileAndRunMultiple) =
//    for{
//      pr <- Promise.make[DockerFailure, CompileAndRunMultipleResult]
//    } yield ConcurrentRunnerTask(task, pr)

  def live(config: Seq[ConcurrentRunnerConfig], queueSize: Int): ZIO[Any, Nothing, ConcurrentRunner] =
    for {
      _ <- ZIO.logInfo(s"Creating concurrent runner with ${config.size} runners. And ${config.map(_.fibersMax).sum} fibers total.")
      q <- Queue.dropping[ConcurrentRunnerTask](queueSize)
      cr = ConcurrentRunner(q, config)
      _ <- cr.startWorkers
    } yield cr

  def layer(config: Seq[ConcurrentRunnerConfig], queueSize: Int) = ZLayer.fromZIO(live(config, queueSize))
  
}