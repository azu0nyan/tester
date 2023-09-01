package tester.srv.controller

import DbViewsShared.AnswerStatus
import DbViewsShared.AnswerStatus.{BeingVerified, Rejected, Verified, VerifiedAwaitingConfirmation}
import doobie.Transactor
import otsbridge.ProblemScore
import tester.srv.controller.AnswerService.{AnswerFilterParams, AnswerStatusUnion, SubmitAnswerResult}
import tester.srv.dao.AnswerDao.{Answer, AnswerMeta}
import tester.srv.dao.AnswerRejectionDao.AnswerRejection
import tester.srv.dao.AnswerReviewDao.AnswerReview
import tester.srv.dao.AnswerVerificationConfirmationDao.AnswerVerificationConfirmation
import tester.srv.dao.AnswerVerificationDao.AnswerVerification
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import zio.*

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
  def deleteAnswer(id: Int): ZIO[Transactor[Task] & AnswerService, Throwable, Boolean] =
    ZIO.serviceWithZIO[AnswerService](_.deleteAnswer(id))
  def filterAnswers(filter: AnswerFilterParams): ZIO[Transactor[Task] & AnswerService, Throwable, Seq[(Answer, AnswerMeta, AnswerStatus)]] =
    ZIO.serviceWithZIO[AnswerService](_.filterAnswers(filter))
  def unconfirmedAnswers(filter: AnswerFilterParams): ZIO[Transactor[Task] & AnswerService, Throwable, Seq[(Answer, AnswerMeta, AnswerStatus)]] =
    ZIO.serviceWithZIO[AnswerService](_.unconfirmedAnswers(filter))
  def submitAnswer(problemId: Int, answerRaw: String): ZIO[Transactor[Task] & AnswerService, Throwable, SubmitAnswerResult] =
    ZIO.serviceWithZIO[AnswerService](_.submitAnswer(problemId, answerRaw))
  def pollAnswerStatus(answerId: Int): ZIO[Transactor[Task] & AnswerService, Throwable, AnswerStatusUnion] =
    ZIO.serviceWithZIO[AnswerService](_.pollAnswerStatus(answerId))
  def confirmAnswer(answerId: Int, userId: Option[Int]): ZIO[Transactor[Task] & AnswerService, Throwable, Boolean] =
    ZIO.serviceWithZIO[AnswerService](_.confirmAnswer(answerId, userId))
  def reviewAnswer(answerId: Int, userId: Int, review: String): ZIO[Transactor[Task] & AnswerService, Throwable, Boolean] =
    ZIO.serviceWithZIO[AnswerService](_.reviewAnswer(answerId, userId, review))
  def rejectAnswer(answerId: Int, message: Option[String], rejectedBy: Option[Int]): ZIO[Transactor[Task] & AnswerService, Throwable, Boolean] =
    ZIO.serviceWithZIO[AnswerService](_.rejectAnswer(answerId, message, rejectedBy))
  def problemAnswers(pId: Int): ZIO[Transactor[Task] & AnswerService, Throwable, Seq[(Answer, AnswerMeta, AnswerStatus)]] =
    ZIO.serviceWithZIO[AnswerService](_.problemAnswers(pId))

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

