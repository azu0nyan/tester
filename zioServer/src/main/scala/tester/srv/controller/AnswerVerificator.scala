package tester.srv.controller

trait AnswerVerificator[F[_]] {
  /**Should cancel previous verification*/
  def verify(problemId: Int, problemTemplate: String, answerId: Int, answerRaw: Int, seed: Int): F[Unit]

}
