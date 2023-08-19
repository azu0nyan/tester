package tester.srv.controller

import EmbeddedPG.EmbeddedPG
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.controller.UserOps.{RegistrationData, RegistrationResult}
import tester.srv.dao.{CourseDao, CourseTemplateDao, ProblemDao}
import tester.srv.dao.CourseTemplateDao.CourseTemplate
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object CourseTest extends ZIOSpecDefault {
  def spec = suite("UserOps test")(
    startCourse,
    stopCourse,
    removeCourse
  ).provideLayer(EmbeddedPG.connectionLayer) @@
    timeout(60.seconds) @@
    withLiveClock

  val createUserMakeTemplate: TranzactIO[Int] =
    val userData = RegistrationData("user", "password", "Aliecbob", "Joens", "a@a.com")
    for {
      userId <- UserOps.registerUser(userData).map(_.asInstanceOf[RegistrationResult.Success].userId)
      _ <- CourseTemplateDao.insert(CourseTemplate("alias", "description", "{}"))
      _ <- CourseTemplateOps.addProblemToTemplateAndUpdateCourses("alias", "problemAlias1")
      _ <- CourseTemplateOps.addProblemToTemplateAndUpdateCourses("alias", "problemAlias2")
    } yield userId


  val startCourse = test("Starting course") {
    val userData = RegistrationData("user", "password", "Aliecbob", "Joens", "a@a.com")
    for {
      userId <- createUserMakeTemplate
      courseId <- Courses.startCourseForUser("alias", userId)
      course <- CourseDao.byId(courseId)
      problems <- ProblemDao.courseProblems(courseId)
      allCourses <- CourseDao.all
      courses <- CourseDao.activeUserCourses(userId)
      _ <- Console.printLine(course)
      _ <- Console.printLine(problems)
      _ <- Console.printLine(allCourses)
      _ <- Console.printLine(courses)
    } yield assertTrue(
      problems.size == 2,
      problems.exists(_.templateAlias == "problemAlias1"),
      problems.exists(_.templateAlias == "problemAlias2"),
      allCourses.size == 1,
      courses.size == 1,
      courses.head.templateAlias == "alias",
      courses.head.startedAt.nonEmpty,
      courses.head.endedAt.isEmpty,
    )
  }

  val stopCourse = test("Stopping course") {
    for{
      userId <- createUserMakeTemplate
      courseId <- Courses.startCourseForUser("alias", userId)
      _ <- Courses.stopCourse("alias", userId)
      active <- CourseDao.activeUserCourses(userId)
      previous <- CourseDao.previousUserCourses(userId)
      courses <- CourseDao.previousUserCourses(userId)
      future <- CourseDao.futureUserCourses(userId)
    } yield assertTrue(
      courses.size == 1,
      future.isEmpty,
      previous.size == 1,
      previous.head.templateAlias == "alias"
    )
  }

  val removeCourse = test("Removing course") {
    for {
      userId <- createUserMakeTemplate
      courseId <- Courses.startCourseForUser("alias", userId)
      _ <- Courses.removeCourseFromUser("alias", userId)
      problems <- ProblemDao.courseProblems(courseId)
      courses <- CourseDao.activeUserCourses(userId)
      finishedCourses <- CourseDao.previousUserCourses(userId)
    } yield assertTrue(
      problems.isEmpty,
      courses.isEmpty,
      finishedCourses.isEmpty,
    )
  }

}
