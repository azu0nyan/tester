package tester.srv.controller

import otsbridge.{AnswerVerificationResult, ProblemTemplate}


trait AnswerVerificator {
  def verifyAnswer(seed: Int, answer: String): AnswerVerificationResult
}
