package tester.srv.controller

trait ProblemVerificatorRegistry[F[_]] {
    def getVerificator(verificatorAlias: String):F[Option[AnswerVerificator]]
}

