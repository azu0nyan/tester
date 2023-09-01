package tester.srv.controller.impl

import DbViewsShared.AnswerStatus
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import io.github.gaelrenoux.tranzactio.doobie.{Connection, Database, TranzactIO, tzio}
import io.github.gaelrenoux.tranzactio.{DbException, doobie}
import tester.srv.controller.{AnswerService, MessageBus, VerificationService}
import tester.srv.controller.AnswerService.*
import tester.srv.dao.AnswerRejectionDao.AnswerRejection
import tester.srv.dao.AnswerReviewDao.AnswerReview
import tester.srv.dao.AnswerVerificationConfirmationDao.AnswerVerificationConfirmation
import tester.srv.dao.AnswerVerificationDao.AnswerVerification
import tester.srv.dao.*
import zio.*
import ProblemDao.Problem
import tester.srv.dao.AnswerDao.{Answer, AnswerMeta}

case class AnswerServiceImpl(
                                    bus: MessageBus,
                                    verificator: VerificationService
                                  ) extends AnswerService {
  override def deleteAnswer(id: Int): TranzactIO[Boolean] =
    AnswerDao.deleteById(id)

  def unconfirmedAnswers(filterParams: AnswerFilterParams): TranzactIO[Seq[(Answer, AnswerMeta, AnswerStatus)]] =
    AnswerDao.unconfirmedAnswers(filterParams).map(l => l.map { case (a, b, c) => (a, b, c.toStatus) })

  def filterAnswers(filter: AnswerFilterParams): TranzactIO[Seq[(Answer, AnswerMeta, AnswerStatus)]] =
    AnswerDao.queryAnswers(filter)().map(l => l.map { case (a, b, c) => (a, b, c.toStatus) })

  override def submitAnswer(problemId: Int, answerRaw: String): TranzactIO[SubmitAnswerResult] = {
    def checkMaxAttempts(problem: Problem): TranzactIO[Boolean] =
      problem.maxAttempts match
        case Some(maxAttempts) =>
          AnswerDao.currentUnrejectedAnswerCount(problemId)
            .map(_ < maxAttempts)
        case None => ZIO.succeed(true)

    val checkNoVerifying: TranzactIO[Boolean] =
      AnswerDao.unverifiedAnswers(problemId).map(_.isEmpty) //todo cache locally

    def submit(p: ProblemDao.Problem): TranzactIO[SubmitAnswerResult] = for {
      id <- AnswerDao.insertReturnId(AnswerDao.Answer(0, problemId, answerRaw, "{}", java.time.Clock.systemUTC().instant()))
      _ <- verificator.verify(problemId, p.templateAlias, id, answerRaw, p.seed, p.requireConfirmation)
      _ <- rejectNotConfirmed(id)
    } yield AnswerSubmitted(id)

    def rejectNotConfirmed(exeptId: Int): TranzactIO[Unit] = ZIO.succeed(()) //todo
    //      for(answs <- AnswerDao.unconfirmedAnswers()) todo

    ProblemDao.byIdOption(problemId).flatMap {
      case Some(problem) =>
        ZIO.ifZIO[doobie.Connection, DbException](checkMaxAttempts(problem))(
          onTrue = ZIO.ifZIO[doobie.Connection, DbException](checkNoVerifying)(
            onTrue = submit(problem),
            onFalse = ZIO.succeed(AlreadyVerifyingAnswer())
          ),
          onFalse = ZIO.succeed(MaximumAttemptsLimitExceeded(problem.maxAttempts.get))
        )
      case None => ZIO.succeed(ProblemNotFound())
    }
  }


  override def pollAnswerStatus(answerId: Int): TranzactIO[AnswerStatusUnion] = //todo do single joined select in answerDao
    for {
      rej <- AnswerRejectionDao.answerRejection(answerId)
      rev <- AnswerReviewDao.answerReview(answerId)
      conf <- AnswerVerificationConfirmationDao.answerConfirmation(answerId)
      ver <- AnswerVerificationDao.answerVerification(answerId)
    } yield AnswerStatusUnion(ver, conf, rej, rev)

  override def confirmAnswer(answerId: Int, userId: Option[Int]): TranzactIO[Boolean] =
    AnswerVerificationConfirmationDao.insert(
      AnswerVerificationConfirmation(answerId, java.time.Clock.systemUTC().instant(), userId))

  override def reviewAnswer(answerId: Int, userId: Int, review: String): TranzactIO[Boolean] =
    AnswerReviewDao.insert(AnswerReview(answerId, review, userId))

  override def rejectAnswer(answerId: Int, message: Option[String], rejectedBy: Option[Int]): TranzactIO[Boolean] =
    AnswerRejectionDao.insert(AnswerRejection(answerId, java.time.Clock.systemUTC().instant(), message, rejectedBy))
}


object AnswerServiceImpl {
  def live: URIO[MessageBus & VerificationService, AnswerService] =
    for {
      bus <- ZIO.service[MessageBus]
      ver <- ZIO.service[VerificationService]
    } yield AnswerServiceImpl(bus, ver)

  def layer: URLayer[MessageBus & VerificationService, AnswerService] = ZLayer.fromZIO(live)
}