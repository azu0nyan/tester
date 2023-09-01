package tester.srv.controller


import EmbeddedPG.EmbeddedPG
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.controller.AnswerService.{AnswerFilterParams, AnswerSubmitted, SubmitAnswerResult}
import tester.srv.controller.UserService.{RegistrationData, RegistrationResult}
import tester.srv.controller.impl.{AnswerServiceImpl, CourseTemplateServiceImpl, CoursesServiceImpl, ProblemServiceImpl, UserServiceImpl, VerificationServiceImpl}
import tester.srv.dao.CourseTemplateDao.CourseTemplate
import tester.srv.dao.{CourseTemplateDao, CourseTemplateProblemDao, UserSessionDao}
import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
import tester.srv.dao.ProblemDao.Problem
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object AnswerServiceTest extends ZIOSpecDefault {

  val bus = MessageBus.layer

  def spec = suite("Answer service test")(
    submitAnswer, submitAnswerRequireConfirm, submitRejectedAnswer, deleteAnswer
  ).provideSomeLayer(EmbeddedPG.connectionLayer)
    .provideSomeLayer(bus) @@
    timeout(60.seconds) @@
    withLiveClock

  def makeService(reg: AnswerVerificatorRegistry): URIO[MessageBus, AnswerService] =
    for {
      prv <- ProblemServiceImpl.live.provideSomeLayer(StubsAndMakers.registryStubLayer)
      ver <- VerificationServiceImpl.live.provideSomeLayer(ZLayer.succeed(reg)).provideSomeLayer(ZLayer.succeed(prv))
      res <- AnswerServiceImpl.live.provideSomeLayer(ZLayer.succeed(ver))
    } yield res


  val submitAnswer = test("Submit answer auto confirm ") {
    for {
      _ <- ZIO.log(s"Making service")
      service <- makeService(StubsAndMakers.acceptAllRegistryStub)
      res <- StubsAndMakers.makeUserAndCourse
      problemId = res._3.find(!_.requireConfirmation).get.id
      submitResult <- service.submitAnswer(problemId, "DUMMY ANSWER")
      id = submitResult.asInstanceOf[AnswerSubmitted].id
      status <- service.pollAnswerStatus(id)
      answs <- service.problemAnswers(problemId)
      unconf <- service.unconfirmedAnswers(AnswerFilterParams())
    } yield assertTrue(
      answs.size == 1,
      answs.exists(a => a._1.id == id),
      unconf.size == 0,
      status.verified.nonEmpty,
      status.verificationConfirmed.nonEmpty,
      status.rejected.isEmpty,
      status.reviewed.isEmpty
    )
  }


  val submitAnswerRequireConfirm = test("Submit answer require confirmation") {
    for {
      service <- makeService(StubsAndMakers.acceptAllRegistryStub)
      res <- StubsAndMakers.makeUserAndCourse
      problemId = res._3.find(_.requireConfirmation).get.id
      submitResult <- service.submitAnswer(problemId, "DUMMY ANSWER")
      id = submitResult.asInstanceOf[AnswerSubmitted].id
      status <- service.pollAnswerStatus(id)
      unconf <- service.unconfirmedAnswers(AnswerFilterParams())
    } yield assertTrue(
      unconf.size == 1,
      status.verified.nonEmpty,
      status.verificationConfirmed.isEmpty,
      status.rejected.isEmpty,
      status.reviewed.isEmpty
    )
  }


  val submitRejectedAnswer = test("Submit rejected answer") {
    for {
      service <- makeService(StubsAndMakers.rejectAllRegistryStub)
      res <- StubsAndMakers.makeUserAndCourse
      problemId = res._3.head.id
      submitResult <- service.submitAnswer(problemId, "DUMMY ANSWER")
      id = submitResult.asInstanceOf[AnswerSubmitted].id
      status <- service.pollAnswerStatus(id)
    } yield assertTrue(
      status.rejected.nonEmpty,
      status.verified.isEmpty,
      status.verificationConfirmed.isEmpty,
      status.reviewed.isEmpty
    )
  }

  val deleteAnswer = test("Delete answer") {
    for {
      service <- makeService(StubsAndMakers.rejectAllRegistryStub)
      res <- StubsAndMakers.makeUserAndCourse
      problemId = res._3.head.id
      submitResult <- service.submitAnswer(problemId, "DUMMY ANSWER")
      delRes <- service.deleteAnswer(submitResult.asInstanceOf[AnswerSubmitted].id)
      answs <- service.problemAnswers(problemId)
    } yield assertTrue(
      delRes,
      answs.isEmpty
    )
  }


}


