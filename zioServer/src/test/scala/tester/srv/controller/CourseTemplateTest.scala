package tester.srv.controller

import EmbeddedPG.EmbeddedPG
import tester.srv.controller.impl.{CourseTemplateServiceTranzactIO, ProblemInfoRegistryZIO, ProblemServiceTranzactIO}
import tester.srv.dao.CourseTemplateDao.CourseTemplate
import tester.srv.dao.{CourseTemplateDao, CourseTemplateProblemDao, UserSessionDao}
import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object CourseTemplateTest extends ZIOSpecDefault {
  def spec = suite("Course template test")(
    courseTemplateCreation,
    courseTemplateAddRemoveProblem
  ).provideLayer(EmbeddedPG.connectionLayer) @@
    timeout(60.seconds) @@
    withLiveClock

 

  val courseTemplateCreation = test("Course template creation"){
    for{
      bus <- MessageBus.make  
      srv <- StubsAndMakers.makeCourseTemplateService(bus)
      _ <- srv.createNewTemplate("alias", "description")
      ct <- CourseTemplateDao.byAliasOption("alias")
    } yield assertTrue(
      ct.nonEmpty,
      ct.get.description == "description",
    )
  }

  val courseTemplateAddRemoveProblem = test("Course template add problem"){
    for{
      bus <- MessageBus.make
      srv <- StubsAndMakers.makeCourseTemplateService(bus)
      _ <- srv.createNewTemplate("alias", "description")
      _ <- srv.addProblemToTemplateAndUpdateCourses("alias", "problemAlias")
      listOne <- srv.templateProblemAliases("alias")
      _ <- srv.removeProblemFromTemplateAndUpdateCourses("alias", "problemAlias")
      listTwo <- srv.templateProblemAliases("alias")
    } yield assertTrue(
      listOne.size == 1,
      listTwo.isEmpty,
      listOne.head.problemAlias == "problemAlias"
    )
  }


}

