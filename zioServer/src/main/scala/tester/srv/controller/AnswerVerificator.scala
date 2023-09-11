package tester.srv.controller


import otsbridge.{AnswerVerificationResult, ProblemTemplate}
import zio.{Task, UIO}


trait AnswerVerificator {
  def verify(seed: Int, answer: String): Task[AnswerVerificationResult]
}
