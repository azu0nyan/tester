package tester.srv.controller.impl

import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import otsbridge.AnswerVerificationResult.*
import otsbridge.AnswerVerificationResult
import tester.srv.controller.MessageBus.AnswerConfirmed
import tester.srv.controller.{AnswerVerificatorRegistry, MessageBus, ProblemService, VerificationService}
import tester.srv.dao.AnswerDao
import tester.srv.dao.AnswerVerificationDao
import tester.srv.dao.AnswerVerificationConfirmationDao
import tester.srv.dao.AnswerRejectionDao
import tester.srv.dao.AnswerRejectionDao.AnswerRejection
import tester.srv.dao.AnswerVerificationConfirmationDao.AnswerVerificationConfirmation
import tester.srv.dao.AnswerVerificationDao.AnswerVerification
import zio.*


case class VerificationServiceTranzactIO(
                                          bus: MessageBus,
                                          registry: AnswerVerificatorRegistry[TranzactIO] ,
                                        ) extends VerificationService[TranzactIO] {
  //CONNECTION LOGIC ETC


  def verify(problemId: Int, verificatorAlias: String, answerId: Int, answerRaw: String, seed: Int, requireConfirmation: Boolean): TranzactIO[Unit] = {
    def processResult(r: AnswerVerificationResult) = r match
      case Verified(score, systemMessage) =>
        for {
          _ <- AnswerVerificationDao.insert(AnswerVerification(answerId, java.time.Clock.systemUTC().instant(), systemMessage, score.toJson, score.percentage))
          _ <- ZIO.when(!requireConfirmation)(
            for{
              _ <- AnswerVerificationConfirmationDao.insert(
                AnswerVerificationConfirmation(answerId, java.time.Clock.systemUTC().instant(), None))
              _ <- bus.publish(AnswerConfirmed(answerId, score, problemId))
            } yield ())
        } yield ()
      case VerificationDelayed(systemMessage) =>
        ZIO.succeed(()) //todo mb remove
      case CantVerify(systemMessage) =>
        AnswerRejectionDao.insert(AnswerRejection(answerId, java.time.Clock.systemUTC().instant(), None, None))

    for {
      verificator <- registry.getVerificator(verificatorAlias)
      _ <- verificator match
        case Some(ver) =>
          for {
            res <- ver.verifyAnswer(seed, answerRaw)
            _ <- processResult(res)
          } yield ()
        case None =>
          AnswerRejectionDao.insert(AnswerRejection(answerId, java.time.Clock.systemUTC().instant(),
            Some(s"Verificator for  $verificatorAlias not found."), None))
    } yield ()

  }
}
