package tester.srv.controller

import zio.*

trait AnswerVerificatorRegistry {
    def getVerificator(alias: String): UIO[Option[AnswerVerificator]]
    def registerVerificator(alias: String, answerVerificator: AnswerVerificator): UIO[Unit]
}