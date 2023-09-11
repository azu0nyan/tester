package tester.srv.controller

import helpers.MultipleRunsProgramProblemTemplate
import otsbridge.{AnswerVerificationResult, DataPack, ProblemTemplate}
import tester.srv.controller.impl.ApplicationImpl
import zio.*
import zioDockerRunner.testRunner.ConcurrentRunner

object DataPackOps {

  import zio.*

  def verificatorFromProblemTemplate(pt: ProblemTemplate, cr: ConcurrentRunner): AnswerVerificator =
    new AnswerVerificator:
      override def verify(seed: RuntimeFlags, answer: String): Task[AnswerVerificationResult] = pt match
        case t: MultipleRunsProgramProblemTemplate =>
          t.verifyAnswerInContainer(seed, answer)
            .provideLayer(ZLayer.succeed(cr))
        case _ => ZIO.attemptBlocking(pt.verifyAnswer(seed, answer))


  //todo use Application trait
  def registerInAppSingle(app: ApplicationImpl, cr: ConcurrentRunner)(dp: DataPack): UIO[Unit] =
    for{
      _ <- ZIO.foreach(dp.courses)(c => app.courseTemplates.registerTemplate(c))
      _ <- ZIO.foreach(dp.problems)(c => app.problems.registerInfo(c))
      _ <- ZIO.foreach(dp.problems)(pt => app.verification.registerVerificator(pt.alias, verificatorFromProblemTemplate(pt, cr)))
    } yield ()

  def registerInApp(app: ApplicationImpl, cr: ConcurrentRunner)(dps: DataPack*): UIO[Unit] =
    for {
      _ <- ZIO.foreach(dps)(dp => registerInAppSingle(app, cr)(dp))
    } yield ()
}
