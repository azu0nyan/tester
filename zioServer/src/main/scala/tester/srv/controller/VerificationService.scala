package tester.srv.controller

trait VerificationService[F[_]] {
  /**Should cancel previous verification*/
  def verify(problemId: Int, problemTemplate: String, answerId: Int, answerRaw: String, seed: Int): F[Unit]

}
