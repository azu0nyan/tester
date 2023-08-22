package tester.srv.controller

import EmbeddedPG.EmbeddedPG
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.controller.UserService.{RegistrationData, RegistrationResult}
import tester.srv.controller.impl.{CourseTemplateTranzactIO, CoursesTranzactIO, UserServiceTranzactIO}
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
    removeCourse,
    addingProblemToTemplate,
    removingProblemToTemplate
  ).provideLayer(EmbeddedPG.connectionLayer) @@
    timeout(60.seconds) @@
    withLiveClock

  val createUserMakeTemplate: TranzactIO[Int] =
    val userData = RegistrationData("user", "password", "Aliecbob", "Joens", "a@a.com")
    for {
      userId <- UserServiceTranzactIO.registerUser(userData).map(_.asInstanceOf[RegistrationResult.Success].userId)
      _ <- CourseTemplateDao.insert(CourseTemplate("alias", "description", "{}"))
      _ <- CourseTemplateTranzactIO.addProblemToTemplateAndUpdateCourses("alias", "problemAlias1")
      _ <- CourseTemplateTranzactIO.addProblemToTemplateAndUpdateCourses("alias", "problemAlias2")
    } yield userId


  val startCourse = test("Starting course") {
    val userData = RegistrationData("user", "password", "Aliecbob", "Joens", "a@a.com")
    for {
      userId <- createUserMakeTemplate
      courseId <- CoursesTranzactIO.startCourseForUser("alias", userId)
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
      courseId <- CoursesTranzactIO.startCourseForUser("alias", userId)
      _ <- CoursesTranzactIO.stopCourse("alias", userId)
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
      courseId <- CoursesTranzactIO.startCourseForUser("alias", userId)
      _ <- CoursesTranzactIO.removeCourseFromUser("alias", userId)
      problems <- ProblemDao.courseProblems(courseId)
      courses <- CourseDao.activeUserCourses(userId)
      finishedCourses <- CourseDao.previousUserCourses(userId)
    } yield assertTrue(
      problems.isEmpty,
      courses.isEmpty,
      finishedCourses.isEmpty,
    )
  }

  val addingProblemToTemplate = test("Adding problem to template adds problem to user"){
    for {
      userId <- createUserMakeTemplate
      courseId <- CoursesTranzactIO.startCourseForUser("alias", userId)
      _ <- CourseTemplateTranzactIO.addProblemToTemplateAndUpdateCourses("alias", "problemAlias3")
      ps <- ProblemDao.courseProblems(courseId)
    } yield assertTrue(
      ps.size == 3,
      ps.exists(_.templateAlias == "problemAlias3")
    )
  }

  val removingProblemToTemplate = test("Removing problem from template removes problem from user") {
    for {
      userId <- createUserMakeTemplate
      courseId <- CoursesTranzactIO.startCourseForUser("alias", userId)
      _ <- CourseTemplateTranzactIO.addProblemToTemplateAndUpdateCourses("alias", "problemAlias3")
      _ <- CourseTemplateTranzactIO.removeProblemFromTemplateAndUpdateCourses("alias", "problemAlias3")
      ps <- ProblemDao.courseProblems(courseId)
    } yield assertTrue(
      ps.size == 2,
      !ps.exists(_.templateAlias == "problemAlias3")
    )
  }


}
