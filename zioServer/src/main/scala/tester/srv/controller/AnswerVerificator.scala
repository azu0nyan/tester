package tester.srv.controller

import otsbridge.{AnswerVerificationResult, ProblemTemplate}
import zio.{Task, UIO}

object AnswerVerificator {
  import zio.*
  def fromProblemTemplate(pt: ProblemTemplate): AnswerVerificator =
    new AnswerVerificator:
      override def verifyAnswer(seed: RuntimeFlags, answer: String): Task[AnswerVerificationResult] =
        ZIO.attemptBlocking(pt.verifyAnswer(seed, answer))
  
}

trait AnswerVerificator {
  def verifyAnswer(seed: Int, answer: String): Task[AnswerVerificationResult]
}
