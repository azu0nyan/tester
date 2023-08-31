package tester.srv.controller

import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.{Database, TranzactIO}
import otsbridge.AnswerField.{AnswerField, TextField}
import otsbridge.{AnswerField, AnswerVerificationResult, ProblemInfo}
import otsbridge.ProblemScore.{BinaryScore, ProblemScore}
import tester.srv.controller.UserService.{RegistrationData, RegistrationResult}
import tester.srv.controller.VerificationService
import tester.srv.controller.impl.{CourseTemplateServiceImpl, CoursesServiceTranzactIO, ProblemInfoRegistryImpl, ProblemServiceImpl, UserServiceImpl}
import tester.srv.dao.CourseTemplateDao
import tester.srv.dao.CourseTemplateDao.CourseTemplate
import tester.srv.dao.ProblemDao.Problem
import zio.*


object StubsAndMakers {

  def acceptAllVerificator = new AnswerVerificator {
    override def verifyAnswer(seed: Int, answer: String): TranzactIO[AnswerVerificationResult] =
      ZIO.succeed(AnswerVerificationResult.Verified(BinaryScore(true), None))
  }

  def acceptAllRegistryStub = new AnswerVerificatorRegistry {
    override def getVerificator(verificatorAlias: String): TranzactIO[Option[AnswerVerificator]] =
      ZIO.succeed(Some(acceptAllVerificator))
    override def registerVerificator(verificatorAlias: String, answerVerificator: AnswerVerificator): ZIO[transactor.Transactor[Task], DbException, Unit] =
      ZIO.succeed(())
  }

  def rejectAllVerificator = new AnswerVerificator {
    override def verifyAnswer(seed: Int, answer: String): TranzactIO[AnswerVerificationResult] =
      ZIO.succeed(AnswerVerificationResult.CantVerify(None))
  }

  def rejectAllRegistryStub = new AnswerVerificatorRegistry {
    override def getVerificator(verificatorAlias: String): TranzactIO[Option[AnswerVerificator]] =
      ZIO.succeed(Some(rejectAllVerificator))

    override def registerVerificator(verificatorAlias: String, answerVerificator: AnswerVerificator): ZIO[transactor.Transactor[Task], DbException, Unit] =
      ZIO.succeed(())
  }

  def makeCourseTemplateService: ZIO[MessageBus, Nothing, CourseTemplateServiceImpl] =
    for {
      res <- ProblemServiceImpl.live
        .provideSomeLayer(ZLayer.fromZIO(StubsAndMakers.registryStub))
    } yield CourseTemplateServiceTranzactIO(res)

  def makeCourseService(bus: MessageBus): ZIO[MessageBus & ProblemService, Nothing, CoursesServiceTranzactIO] =
    for {
      stub <- StubsAndMakers.registryStub
      res <- CoursesServiceTranzactIO.live
    } yield res

  def makeUserAndCourse: TranzactIO[(Int, Int, Seq[Problem])] =
    val userData = RegistrationData("user", "password", "Aliecbob", "Joens", "a@a.com")
    (for {
      bus <- MessageBus.live
      courses <- StubsAndMakers.makeCourseService(bus)
      templates <- StubsAndMakers.makeCourseTemplateService(bus)
      userId <- UserServiceImpl.registerUser(userData).map(_.asInstanceOf[RegistrationResult.Success].userId)
      _ <- templates.createNewTemplate("alias", "description")
      _ <- templates.addProblemToTemplateAndUpdateCourses("alias", "problemAlias1")
      _ <- templates.addProblemToTemplateAndUpdateCourses("alias", "problemAlias2")
      _ <- templates.addProblemToTemplateAndUpdateCourses("alias", "max2")
      _ <- templates.addProblemToTemplateAndUpdateCourses("alias", "require")
      courseId <- courses.startCourseForUser("alias", userId)
      problemIds <- courses.courseProblems(courseId)
    } yield (userId, courseId, problemIds))
      .mapError(_ => DbException.Wrapped(new Exception("")))


  case class ProblemInfoImpl(title: String, alias: String, override val maxAttempts: Option[Int], initialScore: ProblemScore,
                             override val requireConfirmation: Boolean, html: String, answerField: AnswerField) extends ProblemInfo {
    override def title(seed: Int): String = title
    override def problemHtml(seed: Int): String = html
    override def answerField(seed: Int): AnswerField = answerField
  }

  def registryStubLayer = ZLayer.fromZIO(registryStub)
  
  def registryStub = for {
    reg <- ProblemInfoRegistryImpl.live
    _ <- reg.registerProblemInfo(ProblemInfoImpl("title1", "problemAlias1", None, BinaryScore(false), false, "", TextField("")))
    _ <- reg.registerProblemInfo(ProblemInfoImpl("title2", "problemAlias2", None, BinaryScore(false), false, "", TextField("")))
    _ <- reg.registerProblemInfo(ProblemInfoImpl("max 2 attempts", "max2", Some(2), BinaryScore(false), false, "", TextField("")))
    _ <- reg.registerProblemInfo(ProblemInfoImpl("require confirm", "require", None, BinaryScore(false), true, "", TextField("")))
  } yield reg
}
