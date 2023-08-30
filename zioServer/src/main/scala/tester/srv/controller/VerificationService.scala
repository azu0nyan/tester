package tester.srv.controller

trait VerificationService[F[_]] {
  /**Should cancel previous verification*/
  def verify(problemId: Int, verificatorAlias: String, answerId: Int, answerRaw: String, seed: Int, requireConfirmation: Boolean): F[Unit]

}
