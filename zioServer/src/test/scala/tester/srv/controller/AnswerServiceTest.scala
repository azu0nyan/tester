package tester.srv.controller


import EmbeddedPG.EmbeddedPG
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
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
    answerSubmission
  ).provideLayer(EmbeddedPG.connectionLayer) @@
    timeout(60.seconds) @@
    withLiveClock

  def makeService = AnswerServiceTranzactIO(
    VerificationServiceTranzactIO(VerificatiorStubs.acceptAllRegistryStub)
  )

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

  val answerSubmission = test("Answer submission"){
    val srv = makeService
    for{
      res <- createUserAndCourse
      problemId = res._3.head.id
      answerId <- srv.submitAnswer(problemId, "DUMMY ANSWER")
        
    } yield assertTrue(
      true
    )
  }




}


