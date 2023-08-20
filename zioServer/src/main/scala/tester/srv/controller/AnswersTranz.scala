package tester.srv.controller

import zio.*
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import io.github.gaelrenoux.tranzactio.{DbException, doobie}
import doobie.{Connection, Database, TranzactIO, tzio}
import tester.srv.controller.Answers.{AnswerStatus, ProblemNotFound, SubmitAnswerResult}
import tester.srv.dao.{AnswerRejectionDao, AnswerReviewDao, AnswerVerificationConfirmationDao, AnswerVerificationDao, ProblemDao}
import tester.srv.dao.AnswerRejectionDao.AnswerRejection
import tester.srv.dao.AnswerReviewDao.AnswerReview
import tester.srv.dao.AnswerVerificationConfirmationDao.AnswerVerificationConfirmation
import tester.srv.dao.AnswerVerificationDao.AnswerVerification
import tester.srv.dao.tester.srv.dao.AnswerDao

object AnswersTranz extends Answers[TranzactIO] {
  override def deleteAnswer(id: Int): TranzactIO[Boolean] =
    AnswerDao.deleteById(id)

  override def submitAnswer(problemId: Int, answerRaw: String): TranzactIO[SubmitAnswerResult] =
    ProblemDao.byIdOption(problemId).map {
      case Some(problem) =>
        problem.maxAttempts match
          case Some(maxAttempts) => ???
          case None => ??? 
      case None => ProblemNotFound()
    }

  override def pollAnswerStatus(answerId: Int): TranzactIO[AnswerStatus] =
    for {
      rej <- AnswerRejectionDao.answerRejection(answerId)
      rev <- AnswerReviewDao.answerReview(answerId)
      conf <- AnswerVerificationConfirmationDao.answerConfirmation(answerId)
      ver <- AnswerVerificationDao.answerVerification(answerId)
    } yield AnswerStatus(ver, conf, rej, rev)

  override def confirmAnswer(answerId: Int, userId: Option[Int]): TranzactIO[Boolean] =
    AnswerVerificationConfirmationDao.insert(
      AnswerVerificationConfirmation(answerId, java.time.Clock.systemUTC().instant(), userId))

  override def reviewAnswer(answerId: Int, userId: Int, review: String): TranzactIO[Boolean] =
    AnswerReviewDao.insert(AnswerReview(answerID, review, userId))

  override def rejectAnswer(answerId: Int, userId: Int, message: Option[String]): TranzactIO[Boolean] =
    AnswerRejectionDao.insert(AnswerRejection(answerId, java.time.Clock.systemUTC().instant(), message))
}
