package tester.srv.controller

import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import otsbridge.AnswerVerificationResult
import otsbridge.ProblemScore.BinaryScore
import tester.srv.controller.VerificationService
import zio.*


object VerificatiorStubs {

  val acceptAllVerificator = new AnswerVerificator[TranzactIO] {
    override def verifyAnswer(seed: Int, answer: String): TranzactIO[AnswerVerificationResult] =
      ZIO.succeed(AnswerVerificationResult.Verified(BinaryScore(true), None))
  }

  val acceptAllRegistryStub = new AnswerVerificatorRegistry[TranzactIO] {
    override def getVerificator(verificatorAlias: String): TranzactIO[Option[AnswerVerificator[TranzactIO]]] =
      ZIO.succeed(Some(acceptAllVerificator))
  }
}
