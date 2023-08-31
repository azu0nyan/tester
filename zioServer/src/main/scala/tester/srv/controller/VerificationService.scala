package tester.srv.controller

import io.github.gaelrenoux.tranzactio.doobie.TranzactIO

trait VerificationService {
  /**Should cancel previous verification*/
  def verify(problemId: Int, verificatorAlias: String, answerId: Int, answerRaw: String, seed: Int, requireConfirmation: Boolean): TranzactIO[Unit]

}
