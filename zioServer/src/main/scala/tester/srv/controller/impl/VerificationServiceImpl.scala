package tester.srv.controller.impl

import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import otsbridge.AnswerVerificationResult.*
import otsbridge.AnswerVerificationResult
import tester.srv.controller.MessageBus.AnswerConfirmed
import tester.srv.controller.{AnswerVerificator, AnswerVerificatorRegistry, MessageBus, ProblemService, VerificationService}
import tester.srv.dao.AnswerDao
import tester.srv.dao.AnswerVerificationDao
import tester.srv.dao.AnswerVerificationConfirmationDao
import tester.srv.dao.AnswerRejectionDao
import tester.srv.dao.AnswerRejectionDao.AnswerRejection
import tester.srv.dao.AnswerVerificationConfirmationDao.AnswerVerificationConfirmation
import tester.srv.dao.AnswerVerificationDao.AnswerVerification
import zio.*
import zio.logging._


case class VerificationServiceImpl(
                                    bus: MessageBus,
                                    registry: AnswerVerificatorRegistry,
                                    problemService: ProblemService
                                  ) extends VerificationService {
  //CONNECTION LOGIC ETC


  def verify(problemId: Int, verificatorAlias: String, answerId: Int, answerRaw: String, seed: Int, requireConfirmation: Boolean): TranzactIO[Unit] = {
    def processResult(r: AnswerVerificationResult) = r match
      case Verified(score, systemMessage) =>
        for {
          _ <- AnswerVerificationDao.insert(AnswerVerification(answerId, java.time.Clock.systemUTC().instant(), systemMessage, score.toJson, score.percentage))
          _ <- ZIO.when(!requireConfirmation)(
            for {
              _ <- AnswerVerificationConfirmationDao.insert(
                AnswerVerificationConfirmation(answerId, java.time.Clock.systemUTC().instant(), None))
              _ <- problemService.reportAnswerConfirmed(problemId, answerId, score)
              _ <- bus.publish(AnswerConfirmed(problemId, answerId, score))
            } yield ())
        } yield ()
      case VerificationDelayed(systemMessage) =>
        ZIO.succeed(()) //todo mb remove
      case CantVerify(systemMessage) =>
        AnswerRejectionDao.insert(AnswerRejection(answerId, java.time.Clock.systemUTC().instant(), None, None))


    def verifyWith(verificator: AnswerVerificator): TranzactIO[Unit] =
      (for {
        res <- verificator.verifyAnswer(seed, answerRaw)
        _ <- processResult(res)
      } yield ()).catchAllCause { e =>
        for{
          rand <- Random.nextLong.map(_.toHexString)
          _ <- ZIO.logCause(s"Error Id: rand", e)
          _ <- AnswerRejectionDao.insert(AnswerRejection(answerId, java.time.Clock.systemUTC().instant(), Some(s"Error $rand"), None))
        } yield ()
      }

    for {
      verificator <- registry.getVerificator(verificatorAlias)
      _ <- verificator match
        case Some(ver) =>
          verifyWith(ver)
        case None =>
          AnswerRejectionDao.insert(AnswerRejection(answerId, java.time.Clock.systemUTC().instant(),
            Some(s"Verificator for  $verificatorAlias not found."), None))
    } yield ()

  }

  def registerVerificator(alias: String, ver: AnswerVerificator): UIO[Unit] =
    registry.registerVerificator(alias, ver)
      .tap(_ => ZIO.log(s"Registered verificator $alias"))
}


object VerificationServiceImpl {
  def live: URIO[MessageBus & AnswerVerificatorRegistry & ProblemService, VerificationService] =
    for {
      bus <- ZIO.service[MessageBus]
      reg <- ZIO.service[AnswerVerificatorRegistry]
      prb <- ZIO.service[ProblemService]
    } yield VerificationServiceImpl(bus, reg, prb)

  def layer = ZLayer.fromZIO(live)
}
