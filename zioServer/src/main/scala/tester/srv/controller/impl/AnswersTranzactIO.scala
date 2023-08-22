package tester.srv.controller.impl

import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import io.github.gaelrenoux.tranzactio.doobie.{Connection, Database, TranzactIO, tzio}
import io.github.gaelrenoux.tranzactio.{DbException, doobie}
import tester.srv.controller.Answers
import tester.srv.controller.Answers.*
import tester.srv.controller.VerificationService
import tester.srv.dao.AnswerRejectionDao.AnswerRejection
import tester.srv.dao.AnswerReviewDao.AnswerReview
import tester.srv.dao.AnswerVerificationConfirmationDao.AnswerVerificationConfirmation
import tester.srv.dao.AnswerVerificationDao.AnswerVerification
import tester.srv.dao.*
import zio.*
import ProblemDao.Problem

object AnswersTranzactIO extends Answers[TranzactIO] {
  override def deleteAnswer(id: Int): TranzactIO[Boolean] =
    AnswerDao.deleteById(id)

  override def submitAnswer(problemId: Int, answerRaw: String): TranzactIO[SubmitAnswerResult] =
    val verificator: VerificationService[TranzactIO] = ???

    def checkMaxAttempts(problem: Problem): TranzactIO[Boolean] =
      problem.maxAttempts match
        case Some(maxAttempts) =>
          AnswerDao.currentUnrejectedAnswerCount(problemId)
            .map(_ < maxAttempts)
        case None => ZIO.succeed(true)

    val checkNoVerifying: TranzactIO[Boolean] =
      AnswerDao.unverifiedAnswers(problemId).map(_.isEmpty) //todo cache locally

    def submit(p: ProblemDao.Problem): TranzactIO[SubmitAnswerResult] = for{
      id <- AnswerDao.insertReturnId(AnswerDao.Answer(0, problemId, answerRaw, "{}", java.time.Clock.systemUTC().instant()))
      _ <- verificator.verify(problemId,p.templateAlias, id,  answerRaw, p.seed)
      _ <- rejectNotConfirmed
    } yield AnswerSubmitted(id)

    def rejectNotConfirmed: TranzactIO[Unit] = ZIO.succeed(())
//      for(answs <- AnswerDao.unconfirmedAnswers()) todo

    ProblemDao.byIdOption(problemId).flatMap {
      case Some(problem) =>
        ZIO.ifZIO[doobie.Connection,DbException](checkMaxAttempts(problem))(
          onTrue = ZIO.ifZIO[doobie.Connection,DbException](checkNoVerifying)(
            onTrue = submit(problem),
            onFalse = ZIO.succeed(AlreadyVerifyingAnswer())
          ),
          onFalse = ZIO.succeed(MaximumAttemptsLimitExceeded(problem.maxAttempts.get))
        )
      case None => ZIO.succeed(ProblemNotFound())
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
    AnswerReviewDao.insert(AnswerReview(answerId, review, userId))

  override def rejectAnswer(answerId: Int, userId: Int, message: Option[String]): TranzactIO[Boolean] =
    AnswerRejectionDao.insert(AnswerRejection(answerId, java.time.Clock.systemUTC().instant(), message))
}
