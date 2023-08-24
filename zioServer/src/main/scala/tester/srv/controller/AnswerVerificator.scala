package tester.srv.controller

import otsbridge.{AnswerVerificationResult, ProblemTemplate}

object AnswerVerificator {
  import zio.*
  def fromProblemTemplate(pt: ProblemTemplate): AnswerVerificator[Task] = 
    new AnswerVerificator[Task]:
      override def verifyAnswer(seed: RuntimeFlags, answer: String): Task[AnswerVerificationResult] =
        ZIO.attemptBlocking(pt.verifyAnswer(seed, answer))
  
}

trait AnswerVerificator[F[_]] {
  def verifyAnswer(seed: Int, answer: String): F[AnswerVerificationResult]
}
