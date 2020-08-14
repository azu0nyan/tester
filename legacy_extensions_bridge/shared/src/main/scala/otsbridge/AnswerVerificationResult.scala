package otsbridge


sealed trait AnswerVerificationResult {
  val systemMessage: Option[String]
}

case class Verified(score: ProblemScore, review: Option[String], systemMessage: Option[String]) extends AnswerVerificationResult
case class VerificationDelayed(systemMessage:Option[String]) extends AnswerVerificationResult
case class CantVerify(systemMessage: Option[String]) extends AnswerVerificationResult
