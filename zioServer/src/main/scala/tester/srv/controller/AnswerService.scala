package tester.srv.controller

import DbViewsShared.AnswerStatus
import DbViewsShared.AnswerStatus.{BeingVerified, Rejected, Verified, VerifiedAwaitingConfirmation}
import tester.srv.controller.AnswerService.{AnswerFilterParams, AnswerStatusUnion, SubmitAnswerResult}
import tester.srv.dao.AnswerDao.{Answer, AnswerMeta}
import tester.srv.dao.AnswerRejectionDao.AnswerRejection
import tester.srv.dao.AnswerReviewDao.AnswerReview
import tester.srv.dao.AnswerVerificationConfirmationDao.AnswerVerificationConfirmation
import tester.srv.dao.AnswerVerificationDao.AnswerVerification

import java.time.Instant

trait AnswerService[F[_]] {
  def deleteAnswer(id: Int): F[Boolean]

  def unconfirmedAnswers(filter: AnswerFilterParams): F[Seq[(Answer, AnswerMeta, AnswerStatus)]]

  def submitAnswer(problemId: Int, answerRaw: String): F[SubmitAnswerResult]

  def pollAnswerStatus(answerId: Int): F[AnswerStatusUnion]

  def confirmAnswer(answerId: Int, userId: Option[Int]): F[Boolean]

  def reviewAnswer(answerId: Int, userId: Int, review: String): F[Boolean]

  def rejectAnswer(answerId: Int, userId: Int, message: Option[String]): F[Boolean]
}

object AnswerService {

  case class AnswerFilterParams(problemId: Option[Int] = None,
                                problemAlias: Option[String] = None,
                                teacherId: Option[Int] = None,
                                courseAlias: Option[String] = None,
                                groupId: Option[Int] = None,
                                userId: Option[Int] = None)

  case class AnswerStatusUnion(
                                verified: Option[AnswerVerification],
                                verificationConfirmed: Option[AnswerVerificationConfirmation],
                                rejected: Option[AnswerRejection],
                                reviewed: Option[AnswerReview]
                              ) {
    def toStatus: AnswerStatus = rejected match
      case Some(rejection) =>
        Rejected(rejection.message, rejection.rejectedAt)
      case None =>
        verified match
          case Some(verification) =>
            verificationConfirmed match
              case Some(confirmation) =>
                Verified(verification.score, reviewed.map(_.text), verification.systemMessage, verification.verifiedAt, confirmation.confirmedAt)
              case None => VerifiedAwaitingConfirmation(verification.score, verification.systemMessage, verification.verifiedAt)
          case None =>
            BeingVerified()
  }


  sealed trait SubmitAnswerResult
  case class AnswerSubmitted(id: Int) extends SubmitAnswerResult
  case class ProblemNotFound() extends SubmitAnswerResult
  case class MaximumAttemptsLimitExceeded(attempts: Int) extends SubmitAnswerResult
  case class AlreadyVerifyingAnswer() extends SubmitAnswerResult
  case class AnswerSubmissionClosed(cause: Option[String]) extends SubmitAnswerResult

}

