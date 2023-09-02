package tester.srv.controller

import io.github.gaelrenoux.tranzactio.doobie.TranzactIO

trait AnswerVerificatorRegistry {
    def getVerificator(alias: String): TranzactIO[Option[AnswerVerificator]]
    def registerVerificator(alias: String, answerVerificator: AnswerVerificator): TranzactIO[Unit]
}