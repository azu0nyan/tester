package tester.srv.controller

import tester.srv.controller.Answers.AnswerStatus
import tester.srv.controller.Answers.SubmitAnswerResult
import tester.srv.dao.AnswerRejectionDao.AnswerRejection
import tester.srv.dao.AnswerReviewDao.AnswerReview
import tester.srv.dao.AnswerVerificationConfirmationDao.AnswerVerificationConfirmation
import tester.srv.dao.AnswerVerificationDao.AnswerVerification

import java.time.Instant

trait Answers[F[_]]{
  def deleteAnswer(id: Int): F[Boolean]

  def submitAnswer(problemId: Int, answerRaw: String):F[SubmitAnswerResult]

  def pollAnswerStatus(answerId: Int): F[AnswerStatus]

  def confirmAnswer(answerId: Int, userId: Option[Int]): F[Boolean]

  def reviewAnswer(answerId: Int, userId: Int, review: String): F[Boolean]

  def rejectAnswer(answerId: Int, userId: Int, message: Option[String]): F[Boolean]
}

object Answers {

  case class AnswerStatus(
                           verified: Option[AnswerVerification],
                           verificationConfirmed: Option[AnswerVerificationConfirmation],
                           rejected: Option[AnswerRejection],
                           reviewed: Option[AnswerReview]
                         )

  sealed trait SubmitAnswerResult
  case class AnswerSubmitted(id: Int) extends SubmitAnswerResult
  case class ProblemNotFound() extends SubmitAnswerResult
  case class MaximumAttemptsLimitExceeded(attempts: Int) extends SubmitAnswerResult
  case class AlreadyVerifyingAnswer() extends SubmitAnswerResult
  case class AnswerSubmissionClosed(cause: Option[String]) extends SubmitAnswerResult

}

