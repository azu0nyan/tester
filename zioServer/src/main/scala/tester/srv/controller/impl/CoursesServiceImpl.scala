package tester.srv.controller.impl

import DbViewsShared.AnswerStatus.Passing
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.controller.{CourseTemplateRegistry, CoursesService, MessageBus, ProblemService}
import tester.srv.dao.CourseDao.Course
import tester.srv.dao.ProblemDao.Problem
import tester.srv.dao.{CourseDao, CourseTemplateDao, CourseTemplateProblemDao, ProblemDao}
import zio.*

case class CoursesServiceImpl(bus: MessageBus,
                              problemService: ProblemService,
                              templateRegistry: CourseTemplateRegistry
                                   ) extends CoursesService {

  /** Returns courseId */
  def startCourseForUser(alias: String, userId: Int): TranzactIO[Int] =
    for {
      courseTemplate <- CourseTemplateDao.byAliasOption(alias).map(_.get)
      course = CourseDao.Course(0, userId, courseTemplate.alias,
        scala.util.Random.nextInt(), Some(java.time.Clock.systemUTC().instant()), None)
      courseId <- CourseDao.insertReturnId(course)
      aliases <- CourseTemplateProblemDao.templateProblemAliases(courseTemplate.alias)
      _ <- ZIO.foreach(aliases)(a => problemService.startProblem(courseId, a.problemAlias)) //todo add problem service as dependency
    } yield courseId


  def stopCourse(alias: String, userId: Int): TranzactIO[Unit] =
    for {
      c <- CourseDao.byAliasAndUserId(alias, userId)
      _ <- ZIO.when(c.nonEmpty)(CourseDao.setStopped(c.get.id))
    } yield ()

  /** Так же удаляет все ответы пользователя */
  def removeCourseFromUser(alias: String, userId: Int): TranzactIO[Unit] =
    for {
      c <- CourseDao.byAliasAndUserId(alias, userId)
      _ <- ZIO.when(c.nonEmpty)(CourseDao.deleteById(c.get.id))
    } yield ()


  def courseProblems(courseId: Int): TranzactIO[Seq[Problem]] =
    ProblemDao.courseProblems(courseId)

  def byId(courseId: Int): TranzactIO[Course] = CourseDao.byId(courseId)

  def courseViewData(courseId: Int): TranzactIO[viewData.CourseViewData] =
    for{
      course <- byId(courseId)
      template <- templateRegistry.courseTemplate(course.templateAlias).map(_.get) //todo optimize
      problems <- courseProblems(courseId)
      views <- ZIO.foreach(problems)(p => problemService.getViewData(p.id))
    } yield viewData.CourseViewData(courseId.toString, template.courseTitle, Passing(course.endedAt), template.courseData, views, template.description)
}

object CoursesServiceImpl {
  def live: URIO[MessageBus & ProblemService & CourseTemplateRegistry, CoursesService] =
    for{
      bus <- ZIO.service[MessageBus]
      pr <- ZIO.service[ProblemService]
      reg <- ZIO.service[CourseTemplateRegistry]
    } yield CoursesServiceImpl(bus, pr, reg)

  def layer: URLayer[MessageBus & ProblemService & CourseTemplateRegistry, CoursesService] =
    ZLayer.fromZIO(live)
}