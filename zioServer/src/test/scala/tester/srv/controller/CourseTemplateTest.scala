package tester.srv.controller

import EmbeddedPG.EmbeddedPG
import tester.srv.controller.impl.{CourseTemplateServiceImpl, ProblemInfoRegistryImpl, ProblemServiceImpl, UserServiceImpl}
import tester.srv.dao.CourseTemplateDao.CourseTemplate
import tester.srv.dao.{CourseTemplateDao, CourseTemplateProblemDao, UserSessionDao}
import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object CourseTemplateTest extends ZIOSpecDefault {


  val busLayer = MessageBus.layer
  val problemServiceLayer = (busLayer ++ StubsAndMakers.problemRegistryStubLayer) >>> ProblemServiceImpl.layer

  def spec = suite("Course template test")(
    courseTemplateCreation,
    courseTemplateAddRemoveProblem
  ).provideSomeLayer(EmbeddedPG.connectionLayer)
   .provideSomeLayer(busLayer)
   .provideSomeLayer(problemServiceLayer)
   @@
    timeout(60.seconds) @@
    withLiveClock

 

  val courseTemplateCreation = test("Course template creation"){
    for{
      srv <- StubsAndMakers.makeCourseTemplateService
      _ <- srv.createNewTemplate("alias", "description")
      ct <- CourseTemplateDao.byAliasOption("alias")
    } yield assertTrue(
      ct.nonEmpty,
      ct.get.description == "description",
    )
  }

  val courseTemplateAddRemoveProblem = test("Course template add problem"){
    for{
      srv <- StubsAndMakers.makeCourseTemplateService
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

