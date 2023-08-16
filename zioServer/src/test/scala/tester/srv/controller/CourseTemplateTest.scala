package tester.srv.controller

import EmbeddedPG.EmbeddedPG
import tester.srv.controller.CourseTemplateOps.CourseTemplate
import tester.srv.dao.CourseTemplateDao
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object CourseTemplateTest extends ZIOSpecDefault {
  def spec = suite("UserOps test")(
    courseTemplateCreation,
    courseTemplateAddRemoveProblem
  ).provideLayer(EmbeddedPG.connectionLayer) @@
    timeout(60.seconds) @@
    withLiveClock

  val courseTemplateCreation = test("Course template creation"){
    for{
      _ <- CourseTemplateDao.insert(CourseTemplate("alias", "description", "{}"))
      ct <- CourseTemplateDao.byAlias("alias")
    } yield assertTrue(
      ct.nonEmpty,
      ct.get.description == "description",
    )
  }

  val courseTemplateAddRemoveProblem = test("Course template add problem"){
    for{
      _ <- CourseTemplateDao.insert(CourseTemplate("alias", "description", "{}"))
      _ <- CourseTemplateOps.addProblemToTemplateAndUpdateCourses("alias", "problemAlias")
      listOne <- CourseTemplateOps.templateProblemAliases("alias")
      _ <- CourseTemplateOps.removeProblemFromTemplateAndUpdateCourses("alias", "problemAlias")
      listTwo <- CourseTemplateOps.templateProblemAliases("alias")
    } yield assertTrue(
      listOne.size == 1,
      listTwo.isEmpty,
      listOne.head == "problemAlias"
    )
  }


}

