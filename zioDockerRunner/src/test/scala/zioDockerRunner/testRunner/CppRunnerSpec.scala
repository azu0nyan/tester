package zioDockerRunner.testRunner

import zioDockerRunner.dockerIntegration.DockerOps.{CopyArchiveToContainerParams, ExecuteCommandParams, ExecuteCommandResult}
import CompileResult.{CompilationError, CppCompilationSuccess, JavaCompilationSuccess}
import zio.test.ZIOSpecDefault
import RunResult.{RuntimeError, SuccessffulRun, TimeLimitExceeded}
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*
import zioDockerRunner.dockerIntegration.DockerOps
import zioDockerRunner.testRunner.RunResult.{RuntimeError, SuccessffulRun, TimeLimitExceeded}

import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
import scala.io.Source

object CppRunnerSpec extends ZIOSpecDefault {
  def spec = suite("CppRunnerTests")(
    compileProgram,
    compileIncorrectProgram,
    runProgram,
    runTimeLimitExceeded,
  ).provideLayer(DockerOps.dockerClientContextScoped(Commons.testContainerName)) @@ timeout(10.seconds) @@ withLiveClock


  val compileProgram = test("Compiling cpp program with Runner") {
    val cppFileText = Source.fromResource("cpp/echo.cpp").mkString("")

    val toTest = CppRunner
      .compile(ProgramSource(cppFileText))
      .exit

    assertZIO(toTest)(succeeds(equalTo(CppCompilationSuccess("/", "main"))))
  }
  
  val compileIncorrectProgram = test("Compiling incorrect cpp program with Runner") {
    val cppFileText = Source.fromResource("cpp/uncompillable.cpp").mkString("")

    val toTest = CppRunner
      .compile(ProgramSource(cppFileText))
      .exit
    
    assertZIO(toTest)(failsWithA[CompilationError])
  }
  
  val runProgram = test("Running program and getting correct output") {
    val cppFileText = Source.fromResource("cpp/echo.cpp").mkString("")
    
    for{
      cs <- CppRunner.compile(ProgramSource(cppFileText))
      exit <- CppRunner.runCompiled(cs, "322", 10_000).exit
    } yield assert(exit)(succeeds(isSubtype[SuccessffulRun](hasField("output", _.output, equalTo("322")))))
  }
  
  val runTimeLimitExceeded = test("Running program with time limit exceeded") {
    val cppFileText =Source.fromResource("cpp/whiletrue.cpp").mkString("")
    
    for{
      cs <- CppRunner.compile(ProgramSource(cppFileText))
      exit <- CppRunner.runCompiled(cs, "", 500).exit
    } yield assert(exit)(succeeds(isSubtype[TimeLimitExceeded](anything)))
  }
}
