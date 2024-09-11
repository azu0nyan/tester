package tester.srv.controller.impl

import DbViewsShared.CourseStatus.Passing
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.controller.{CourseTemplateRegistry, CoursesService, MessageBus, ProblemService}
import tester.srv.dao.CourseDao.Course
import tester.srv.dao.ProblemDao.Problem
import tester.srv.dao.{CourseDao, CourseTemplateProblemDao, DbCourseTemplateDao, ProblemDao}
import zio.*

case class CoursesServiceImpl(bus: MessageBus,
                              problemService: ProblemService,
                              templateRegistry: CourseTemplateRegistry
                             ) extends CoursesService {

  //todo bind course to Option[Group] respect these binding in get scores
  /** Returns courseId */
  def startCourseForUser(alias: String, userId: Int): TranzactIO[Int] =
    for {
      courseTemplate <- DbCourseTemplateDao.byAliasOption(alias).map(_.get)
      course = CourseDao.Course(0, userId, courseTemplate.alias,
        scala.util.Random.nextInt(), Some(java.time.Clock.systemUTC().instant()), None)
      courseId <- CourseDao.insertReturnId(course)
      aliases <- CourseTemplateProblemDao.templateProblemAliases(courseTemplate.alias)
      _ <- ZIO.foreach(aliases)(a => problemService.startProblem(courseId, a.problemAlias))
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

  def byId(courseId: Int): TranzactIO[Option[Course]] = CourseDao.byIdOption(courseId)

  def courseViewData(courseId: Int): TranzactIO[Option[viewData.CourseViewData]] =
    for {
      courseOpt <- byId(courseId)
      templateOpt <- ZIO.foreach(courseOpt)(course => templateRegistry.courseTemplate(course.templateAlias))
      problems <- courseProblems(courseId)
      views <- ZIO.foreach(problems)(p => problemService.getViewData(p.id)) //todo optimize
    } yield templateOpt.flatten.map(template =>
      viewData.CourseViewData(courseId.toString, template.courseTitle, Passing(courseOpt.get.endedAt),
        template.courseData, views.flatten, template.description))

  def partialCourseViewData(courseId: Int): TranzactIO[Option[viewData.PartialCourseViewData]] =
    for {
      courseOpt <- byId(courseId)
      templateOpt <- ZIO.foreach(courseOpt)(course => templateRegistry.courseTemplate(course.templateAlias))
      problems <- courseProblems(courseId)
      views <- ZIO.foreach(problems)(p => problemService.getRefViewData(p.id)) //todo optimize
    } yield templateOpt.flatten.map(template =>
      viewData.PartialCourseViewData(courseId.toString, template.courseTitle, template.description,
        Passing(courseOpt.get.endedAt), template.courseData.toJson, views.flatten)
    )

  def userCourses(userId: Int): TranzactIO[Seq[viewData.CourseInfoViewData]] =
    for {
      courses <- CourseDao.userCourses(userId)
      res <- ZIO.foreach(courses)(course =>
        for{
          t <- templateRegistry.courseTemplate(course.templateAlias)
        } yield t.map( t => viewData.CourseInfoViewData(course.id.toString, t.courseTitle, Passing(course.endedAt), t.description))
      )
    } yield res.flatten
}

object CoursesServiceImpl {
  def live: URIO[MessageBus & ProblemService & CourseTemplateRegistry, CoursesService] =
    for {
      bus <- ZIO.service[MessageBus]
      pr <- ZIO.service[ProblemService]
      reg <- ZIO.service[CourseTemplateRegistry]
    } yield CoursesServiceImpl(bus, pr, reg)

  def layer: URLayer[MessageBus & ProblemService & CourseTemplateRegistry, CoursesService] =
    ZLayer.fromZIO(live)
}