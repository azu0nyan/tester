package tester.srv.controller

import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.{Database, TranzactIO}
import otsbridge.AnswerField.{AnswerField, TextField}
import otsbridge.{AnswerField, AnswerVerificationResult, ProblemInfo}
import otsbridge.ProblemScore.{BinaryScore, ProblemScore}
import tester.srv.controller.UserService.{RegistrationData, RegistrationResult}
import tester.srv.controller.VerificationService
import tester.srv.controller.impl.{CourseTemplateRegistryImpl, CourseTemplateServiceImpl, CoursesServiceImpl, ProblemInfoRegistryImpl, ProblemServiceImpl, UserServiceImpl}
import tester.srv.dao.CourseTemplateDao
import tester.srv.dao.CourseTemplateDao.CourseTemplate
import tester.srv.dao.ProblemDao.Problem
import zio.*


object StubsAndMakers {

  def acceptAllVerificator = new AnswerVerificator {   
    override def verifyAnswer(seed: Int, answer: String): Task[AnswerVerificationResult] =
      ZIO.succeed(AnswerVerificationResult.Verified(BinaryScore(true), None))
  }

  def acceptAllRegistryStub = new AnswerVerificatorRegistry {
    override def getVerificator(verificatorAlias: String): TranzactIO[Option[AnswerVerificator]] =
      ZIO.succeed(Some(acceptAllVerificator))
    override def registerVerificator(verificatorAlias: String, answerVerificator: AnswerVerificator): ZIO[transactor.Transactor[Task], DbException, Unit] =
      ZIO.succeed(())
  }

  def rejectAllVerificator = new AnswerVerificator {
  
    override def verifyAnswer(seed: Int, answer: String): Task[AnswerVerificationResult] =
      ZIO.succeed(AnswerVerificationResult.CantVerify(None))
  }

  def rejectAllRegistryStub = new AnswerVerificatorRegistry {
    override def getVerificator(verificatorAlias: String): TranzactIO[Option[AnswerVerificator]] =
      ZIO.succeed(Some(rejectAllVerificator))

    override def registerVerificator(verificatorAlias: String, answerVerificator: AnswerVerificator): ZIO[transactor.Transactor[Task], DbException, Unit] =
      ZIO.succeed(())
  }

  def makeCourseTemplateService: ZIO[MessageBus & ProblemService, Nothing, CourseTemplateService] =
    for {
      srvv <- CourseTemplateServiceImpl.live.provideSomeLayer(courseTemplateRegistryLayer)
    } yield srvv

  def makeProblemService: ZIO[MessageBus, Nothing, ProblemService] =
  for{
    stub <- problemRegistryStub
    res <-  ProblemServiceImpl.live.provideSomeLayer(ZLayer.succeed(stub))
  } yield res

  def makeCourseService: ZIO[MessageBus & ProblemService, Nothing, CoursesService] =
    for {
      stub <- StubsAndMakers.courseTemplateRegistryStub
      res <- CoursesServiceImpl.live.provideSomeLayer(ZLayer.succeed(stub))
    } yield res

  def makeUserAndCourse: TranzactIO[(Int, Int, Seq[Problem])] =
    val userData = RegistrationData("user", "password", "Aliecbob", "Joens", "a@a.com")
    (for {
      bus <- MessageBus.live
      userService <- UserServiceImpl.live.provideSomeLayer(ZLayer.succeed(bus))
      probems <- StubsAndMakers.makeProblemService.provideSomeLayer(ZLayer.succeed(bus))
      courses <- StubsAndMakers.makeCourseService.provideSomeLayer(ZLayer.succeed(bus)).provideSomeLayer(ZLayer.succeed(probems))
      templates <- StubsAndMakers.makeCourseTemplateService.provideSomeLayer(ZLayer.succeed(bus)).provideSomeLayer(ZLayer.succeed(probems))
      userId <- UserService.registerUser(userData).map(_.asInstanceOf[RegistrationResult.Success].userId).provideSomeLayer(ZLayer.succeed(userService))
      _ <- templates.createNewTemplate("alias", "description")
      _ <- templates.addProblemToTemplateAndUpdateCourses("alias", "problemAlias1")
      _ <- templates.addProblemToTemplateAndUpdateCourses("alias", "problemAlias2")
      _ <- templates.addProblemToTemplateAndUpdateCourses("alias", "max2")
      _ <- templates.addProblemToTemplateAndUpdateCourses("alias", "require")
      courseId <- courses.startCourseForUser("alias", userId)
      problemIds <- courses.courseProblems(courseId)
    } yield (userId, courseId, problemIds))
      .tapErrorCause(e => ZIO.logErrorCause(e))
      .mapError(_ => DbException.Wrapped(new Exception("")))


  case class ProblemInfoImpl(title: String, alias: String, override val maxAttempts: Option[Int], initialScore: ProblemScore,
                             override val requireConfirmation: Boolean, html: String, answerField: AnswerField) extends ProblemInfo {
    override def title(seed: Int): String = title
    override def problemHtml(seed: Int): String = html
    override def answerField(seed: Int): AnswerField = answerField
  }


  def courseTemplateRegistryLayer = ZLayer.fromZIO(courseTemplateRegistryStub)
  def courseTemplateRegistryStub = for{
    reg <- CourseTemplateRegistryImpl.live
    _ <- reg.registerCourseTemplate(otsbridge.CourseTemplate.CourseTemplateData("someCourse", problemAliases = Seq("problemAlias1", "problemAlias2")))
  } yield reg


  def problemRegistryStubLayer = ZLayer.fromZIO(problemRegistryStub)

  def problemRegistryStub = for {
    reg <- ProblemInfoRegistryImpl.live
    _ <- reg.registerProblemInfo(ProblemInfoImpl("title1", "problemAlias1", None, BinaryScore(false), false, "", TextField("")))
    _ <- reg.registerProblemInfo(ProblemInfoImpl("title2", "problemAlias2", None, BinaryScore(false), false, "", TextField("")))
    _ <- reg.registerProblemInfo(ProblemInfoImpl("max 2 attempts", "max2", Some(2), BinaryScore(false), false, "", TextField("")))
    _ <- reg.registerProblemInfo(ProblemInfoImpl("require confirm", "require", None, BinaryScore(false), true, "", TextField("")))
  } yield reg
}
