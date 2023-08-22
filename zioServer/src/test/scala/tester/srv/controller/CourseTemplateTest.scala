package tester.srv.controller

import EmbeddedPG.EmbeddedPG
import tester.srv.controller.impl.CourseTemplateTranzactIO
import tester.srv.dao.CourseTemplateDao.CourseTemplate
import tester.srv.dao.{CourseTemplateDao, CourseTemplateProblemDao, UserSessionDao}
import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
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
      ct <- CourseTemplateDao.byAliasOption("alias")
    } yield assertTrue(
      ct.nonEmpty,
      ct.get.description == "description",
    )
  }

  val courseTemplateAddRemoveProblem = test("Course template add problem"){
    for{
      _ <- CourseTemplateDao.insert(CourseTemplate("alias", "description", "{}"))
      _ <- CourseTemplateTranzactIO.addProblemToTemplateAndUpdateCourses("alias", "problemAlias")
      listOne <- CourseTemplateProblemDao.templateProblemAliases("alias")
      _ <- CourseTemplateTranzactIO.removeProblemFromTemplateAndUpdateCourses("alias", "problemAlias")
      listTwo <- CourseTemplateProblemDao.templateProblemAliases("alias")
    } yield assertTrue(
      listOne.size == 1,
      listTwo.isEmpty,
      listOne.head.problemAlias == "problemAlias"
    )
  }


}

