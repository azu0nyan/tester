package zioDockerRunner.testRunner

import zio.*
import zioDockerRunner.dockerIntegration.{CompressOps, DockerOps}
import zioDockerRunner.testRunner.CompileResult.{CompilationError, CppCompilationSuccess}
import otsbridge.ProgrammingLanguage
import otsbridge.ProgrammingLanguage.*
import zioDockerRunner.dockerIntegration.DockerOps.{DockerClientContext, RunningContainerFailure}
import zioDockerRunner.testRunner.RunResult.{RuntimeError, SuccessffulRun, TimeLimitExceeded}

import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
given CppRunner: LanguageRunner[ProgrammingLanguage.Cpp.type] with {
  override type CompilationSuccessL = CppCompilationSuccess
  override def compile(source: ProgramSource): ZIO[DockerOps.DockerClientContext, RunningContainerFailure | CompilationFailure, CppCompilationSuccess] =
    for{
      tarStream <- ZIO.succeed(CompressOps.asTarStream(source.src, "main.cpp"))
      _ <- DockerOps.copyArchiveToContainer(DockerOps.CopyArchiveToContainerParams("/", tarStream))
      compileRes <- DockerOps.executeCommandInContainer(DockerOps.ExecuteCommandParams(Seq("g++", "main.cpp", "-o", "main"), None))
      r <-
        if (compileRes.exitCode.contains(1)) ZIO.fail(CompilationError(compileRes.stdOut))
        else ZIO.succeed(CppCompilationSuccess("/", "main"))
    } yield r
  override def runCompiled(compilationSuccess: CppCompilationSuccess, input: String, maxTime: Long): ZIO[DockerClientContext, RunningContainerFailure, RawRunResult] = {
    val inputStream = new ByteArrayInputStream(input.getBytes("UTF-8"))
    val runCommand = Seq("./main")

    val run: ZIO[DockerOps.DockerClientContext, RunningContainerFailure, RawRunResult] = (for {
      startTime <- Clock.currentTime(TimeUnit.MILLISECONDS)
      runRes <- DockerOps.executeCommandInContainer(DockerOps.ExecuteCommandParams(runCommand, Some(inputStream))).disconnect
      endTime <- Clock.currentTime(TimeUnit.MILLISECONDS)
    } yield if (runRes.exitCode.contains(1)) RuntimeError(runRes.stdOut) else SuccessffulRun(runRes.stdOut, endTime - startTime))

    run.timeoutTo[RawRunResult](TimeLimitExceeded(maxTime))(x => x)(maxTime.milliseconds)
  }

}