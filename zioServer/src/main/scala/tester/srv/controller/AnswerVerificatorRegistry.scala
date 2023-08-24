package tester.srv.controller

trait AnswerVerificatorRegistry[F[_]] {
    def getVerificator(verificatorAlias: String):F[Option[AnswerVerificator[F]]]
}

