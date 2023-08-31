package tester.srv.controller

trait AnswerVerificatorRegistry[F[_]] {
    def getVerificator(verificatorAlias: String):F[Option[AnswerVerificator[F]]]
    def registerVerificator(verificatorAlias: String, answerVerificator: AnswerVerificator[F]): F[Unit]
}