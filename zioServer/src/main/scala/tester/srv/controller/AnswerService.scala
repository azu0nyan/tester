package tester.srv.controller

import DbViewsShared.AnswerStatus
import DbViewsShared.AnswerStatus.{BeingVerified, Rejected, Verified, VerifiedAwaitingConfirmation}
import otsbridge.ProblemScore
import tester.srv.controller.AnswerService.{AnswerFilterParams, AnswerStatusUnion, SubmitAnswerResult}
import tester.srv.dao.AnswerDao.{Answer, AnswerMeta}
import tester.srv.dao.AnswerRejectionDao.AnswerRejection
import tester.srv.dao.AnswerReviewDao.AnswerReview
import tester.srv.dao.AnswerVerificationConfirmationDao.AnswerVerificationConfirmation
import tester.srv.dao.AnswerVerificationDao.AnswerVerification
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO

import java.time.Instant

trait AnswerService {
  def deleteAnswer(id: Int): TranzactIO[Boolean]

  def filterAnswers(filter: AnswerFilterParams): TranzactIO[Seq[(Answer, AnswerMeta, AnswerStatus)]]

  def unconfirmedAnswers(filter: AnswerFilterParams): TranzactIO[Seq[(Answer, AnswerMeta, AnswerStatus)]]

  def submitAnswer(problemId: Int, answerRaw: String): TranzactIO[SubmitAnswerResult]

  def pollAnswerStatus(answerId: Int): TranzactIO[AnswerStatusUnion]

  def confirmAnswer(answerId: Int, userId: Option[Int]): TranzactIO[Boolean]

  def reviewAnswer(answerId: Int, userId: Int, review: String): TranzactIO[Boolean]

  def rejectAnswer(answerId: Int, message: Option[String], rejectedBy: Option[Int]): TranzactIO[Boolean]

  def problemAnswers(pId: Int): TranzactIO[Seq[(Answer, AnswerMeta, AnswerStatus)]] =
    filterAnswers(AnswerFilterParams(problemId = Some(pId)))
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
                Verified(ProblemScore.fromJson(verification.score), reviewed.map(_.text), verification.systemMessage, verification.verifiedAt, confirmation.confirmedAt)
              case None => VerifiedAwaitingConfirmation(ProblemScore.fromJson(verification.score), verification.systemMessage, verification.verifiedAt)
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

