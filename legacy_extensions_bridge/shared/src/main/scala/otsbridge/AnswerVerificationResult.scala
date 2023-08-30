package otsbridge

import otsbridge.ProblemScore.ProblemScore


sealed trait AnswerVerificationResult {
  val systemMessage: Option[String]
}

object AnswerVerificationResult{
  case class Verified(score: ProblemScore, systemMessage: Option[String]) extends AnswerVerificationResult
  case class VerificationDelayed(systemMessage: Option[String]) extends AnswerVerificationResult
  case class CantVerify(systemMessage: Option[String]) extends AnswerVerificationResult  
}


