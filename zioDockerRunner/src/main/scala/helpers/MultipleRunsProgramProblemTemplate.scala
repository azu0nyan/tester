package helpers

import helpers.MultipleRunsProgramProblemTemplate.maxSystemOrErrorMessageLength
import otsbridge.AnswerField.*
import otsbridge.ProblemScore.*
import otsbridge.ProgrammingLanguage
import otsbridge.ProgrammingLanguage.*
import otsbridge.AnswerVerificationResult.*
import otsbridge.{AnswerVerificationResult, ProblemScore, ProblemTemplate}
import zioDockerRunner.dockerIntegration.DockerOps
import zioDockerRunner.dockerIntegration.DockerOps.DockerFailure
import zioDockerRunner.testRunner.CompileResult.*
import zioDockerRunner.testRunner.{CompilationFailure, CompileAndRunMultiple, ConcurrentRunner, HardwareLimitations, ProgramSource, SingleRun}


object MultipleRunsProgramProblemTemplate {
  val maxSystemOrErrorMessageLength = 5000
}

trait MultipleRunsProgramProblemTemplate extends ProblemTemplate {
  def runs(seed: Int): Seq[SingleRun]

  val limitationsInfo: HardwareLimitations = HardwareLimitations()

  override val initialScore: ProblemScore.ProblemScore = BinaryScore(false)

  def modifyProgram(seed: Int, answ: String, language: ProgrammingLanguage): String = answ

  override def answerField(seed: Int): ProgramInTextField

  override def verifyAnswer(seed: Int, answer: String): AnswerVerificationResult = {
    /*  answerField(seed).answerFromString(answer) match {
        case Some(ProgramAnswer(program, programmingLanguage)) =>
          val modifyied = modifyProgram( seed,program, programmingLanguage)
          CompileAndRun.compileAndRunMultiple(modifyied, programmingLanguage, runs(seed), limitationsInfo) match {
            case Left(CompilationError(errorMessage)) => CantVerify(Some(if(errorMessage.length == 0)"Compilation error" else errorMessage.take(maxSystemOrErrorMessageLength)))
            case Left(CompilationServerError(msg)) => VerificationDelayed(Some(msg.getOrElse("Compilation server error")))
            case Left(RemoteWorkerConnectionError()) => VerificationDelayed(Some("Remote worker connection error"))
            case Left(RemoteWorkerCreationError()) => VerificationDelayed(Some("Cant create remote worker"))
            case Right(score) => Verified(score, None)
          }
        case _ =>
          log.info(s"Undecodable answe $answer")
          CantVerify(Some("Cant decode answer"))
      }*/
    CantVerify(Some("Use ZIO verificator"))
  }

  import zio.*

  def verifyAnswerInContainer(seed: Int, answer: String): URIO[ConcurrentRunner, AnswerVerificationResult] = {
    answerField(seed).answerFromString(answer) match
      case Some(ProgramAnswer(program, programmingLanguage)) =>
        val modifyied = modifyProgram(seed, program, programmingLanguage)
        val crm = CompileAndRunMultiple(ProgramSource(modifyied), programmingLanguage, runs(seed), limitationsInfo)
        val res = for {
          runner <- ZIO.service[ConcurrentRunner]
          promise <- runner.addTask(crm)
          res <- promise.await
        } yield res match {
          case s: MultipleRunsResultScore => Verified(s, None)
          case CompilationError(errorMessage: String) =>
            CantVerify(Some(if (errorMessage.isEmpty) "Compilation error" else errorMessage.take(maxSystemOrErrorMessageLength)))
        }
        res.catchAll {
          case DockerFailure.CantCopyToContainer(errorMessage) =>
            ZIO.logError(errorMessage.orElse(Some("")).map(m => s"Can't copy to docker container. $m").get)
              .map(_ => VerificationDelayed(Some("Can't copy to docker container")))
          case DockerFailure.CantExecuteCommand(errorMessage) =>
            ZIO.logError(errorMessage.orElse(Some("")).map(m => s"Can't execute command in docker. $m").get)
              .map(_ => VerificationDelayed(Some("Can't execute command in docker")))
          case DockerFailure.UnknownDockerFailure =>
            ZIO.logError(s"Unknown docker error")
              .map(_ => VerificationDelayed(Some("Unknown docker error")))
          case DockerFailure.WorkQueueIsFull =>
            ZIO.logWarning(s"Worker queue is full")
              .map(_ =>VerificationDelayed(Some("Очередь проверки переполнена")))
          case DockerFailure.CantCreateClient(errorMessage) =>
            ZIO.logError(errorMessage.orElse(Some("")).map(m => s"Can't create docker client. $m").get)
              .map(_ => VerificationDelayed(Some("Can't create docker client")))
          case DockerFailure.CantCreateContainer(errorMessage) =>
            ZIO.logError(errorMessage.orElse(Some("")).map(m => s"Can't create container worker. $m").get)
              .map(_ => VerificationDelayed(Some("Can't create container worker")))
        }
      case None =>
        ZIO.succeed(CantVerify(Some(s"Cant decode answer: $answer")))


  }

}


