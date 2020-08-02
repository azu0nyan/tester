package otsbridge


sealed trait SubmissionResult {
  val systemMessage: Option[String]
}

case class Verified(score: ProblemScore, review: Option[String], systemMessage: Option[String]) extends SubmissionResult
case class VerificationDelayed(systemMessage:Option[String]) extends SubmissionResult
case class CantVerify(systemMessage: Option[String]) extends SubmissionResult
