package tester.srv.controller


import EmbeddedPG.EmbeddedPG
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.controller.AnswerService.{AnswerFilterParams, AnswerSubmitted, SubmitAnswerResult}
import tester.srv.controller.UserService.{RegistrationData, RegistrationResult}
import tester.srv.controller.impl.{AnswerServiceTranzactIO, CourseTemplateTranzactIO, CoursesTranzactIO, UserServiceTranzactIO, VerificationServiceTranzactIO}
import tester.srv.dao.CourseTemplateDao.CourseTemplate
import tester.srv.dao.{CourseTemplateDao, CourseTemplateProblemDao, UserSessionDao}
import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
import tester.srv.dao.ProblemDao.Problem
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object AnswerServiceTest extends ZIOSpecDefault {
  def spec = suite("Answer service test")(
    submitAnswer, submitRejectedAnswer, deleteAnswer
  ).provideLayer(EmbeddedPG.connectionLayer) @@
    timeout(60.seconds) @@
    withLiveClock

  def makeService(reg: AnswerVerificatorRegistry[TranzactIO]): UIO[AnswerServiceTranzactIO] = ZIO.succeed(AnswerServiceTranzactIO(
    VerificationServiceTranzactIO(reg)
  ))

  val createUserAndCourse: TranzactIO[(Int, Int, Seq[Problem])] =
    val userData = RegistrationData("user", "password", "Aliecbob", "Joens", "a@a.com")
    val template = CourseTemplate("alias", "description", "{}")
    for {
      userId <- UserServiceTranzactIO.registerUser(userData).map(_.asInstanceOf[RegistrationResult.Success].userId)
      _ <- CourseTemplateDao.insert(template)
      _ <- CourseTemplateTranzactIO.addProblemToTemplateAndUpdateCourses("alias", "problemAlias1")
      _ <- CourseTemplateTranzactIO.addProblemToTemplateAndUpdateCourses("alias", "problemAlias2")
      courseId <- CoursesTranzactIO.startCourseForUser(template.alias, userId)
      problemIds <- CoursesTranzactIO.courseProblems(courseId)
    } yield (userId, courseId, problemIds)


  val submitAnswer = test("Answer submission") {
    for {
      service <- makeService(VerificatiorStubs.acceptAllRegistryStub)
      res <- createUserAndCourse
      problemId = res._3.head.id
      submitResult <- service.submitAnswer(problemId, "DUMMY ANSWER")
      id = submitResult.asInstanceOf[AnswerSubmitted].id
      status <- service.pollAnswerStatus(id)
      answs <- service.problemAnswers(problemId)
    } yield assertTrue(
      answs.size == 1,
      answs.exists(a => a._1.id == id),
      status.verified.nonEmpty,
      status.verificationConfirmed.nonEmpty,
      status.rejected.isEmpty,
      status.reviewed.isEmpty
    )
  }


  val submitRejectedAnswer = test("Submit rejected answer") {
    for {
      service <- makeService(VerificatiorStubs.rejectAllRegistryStub)
      res <- createUserAndCourse
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
      service <- makeService(VerificatiorStubs.rejectAllRegistryStub)
      res <- createUserAndCourse
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


