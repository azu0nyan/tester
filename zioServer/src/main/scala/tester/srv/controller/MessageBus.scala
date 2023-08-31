package tester.srv.controller

import otsbridge.ProblemScore.ProblemScore
import tester.srv.controller.MessageBus.*
import zio.*
import zio.stream.{Take, ZSink, ZStream}

import java.time.Instant

case class MessageBus(
                       main: Hub[Message],
                       answerConfirmations: Hub[AnswerConfirmed],
                       userLogins: Hub[UserLoggedIn],
                     ) {
  def publish(m: Message): UIO[Boolean] = main.publish(m)
}

object MessageBus {
  sealed trait Message
  case class AnswerConfirmed(answerId: Int, score: ProblemScore, problemId: Int) extends Message
  case class UserLoggedIn(userId: Int, at: Instant) extends Message


  def make: UIO[MessageBus] =
    for {
      main <- Hub.unbounded[Message]
      answerConfirmations <- Hub.unbounded[AnswerConfirmed]
      userLogins <- Hub.unbounded[ UserLoggedIn]
      _ <- ZStream.fromHub(main).collect { case a: AnswerConfirmed => a }.run(ZSink.fromHub(answerConfirmations))
      _ <- ZStream.fromHub(main).collect { case a: UserLoggedIn => a }.run(ZSink.fromHub(userLogins))
    } yield MessageBus(main, answerConfirmations, userLogins)
}
