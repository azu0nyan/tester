package tester.srv.controller

import io.github.gaelrenoux.tranzactio.doobie.TranzactIO

trait AnswerVerificatorRegistry {
    def getVerificator(verificatorAlias: String): TranzactIO[Option[AnswerVerificator]]
    def registerVerificator(verificatorAlias: String, answerVerificator: AnswerVerificator): TranzactIO[Unit]
}