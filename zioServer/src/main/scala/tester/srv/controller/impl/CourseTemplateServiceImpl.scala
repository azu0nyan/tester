package tester.srv.controller.impl

import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import otsbridge.CoursePiece.CourseRoot
import otsbridge.CourseTemplate
import tester.srv.controller.{CourseTemplateRegistry, CourseTemplateService, MessageBus, ProblemService}
import tester.srv.dao.DbCourseTemplateDao.DbCourseTemplate
import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
import tester.srv.dao.{CourseDao, DbCourseTemplateDao, CourseTemplateProblemDao}
import zio.*

case class CourseTemplateServiceImpl(
                                      bus: MessageBus,
                                      registryImpl: CourseTemplateRegistry,
                                      problemService: ProblemService
                                    ) extends CourseTemplateService {

  def templateProblemAliases(alias: String): TranzactIO[Seq[CourseTemplateProblem]] =
    CourseTemplateProblemDao.templateProblemAliases(alias)

  def createNewTemplate(alias: String, description: String): TranzactIO[Boolean] =
    DbCourseTemplateDao.insert(DbCourseTemplateDao.DbCourseTemplate(alias, description, otsbridge.CoursePiece.CourseRoot(alias, "", Seq()).toJson))

  def addProblemToTemplateAndUpdateCourses(courseAlias: String, problemAlias: String): TranzactIO[Boolean] =
    for {
      res <- CourseTemplateProblemDao.insert(CourseTemplateProblem(courseAlias, problemAlias))
      courses <- CourseDao.linkedToTemplateCourses(courseAlias)
      _ <- ZIO.foreach(courses)(course => problemService.startProblem(course.id, problemAlias))
    } yield res


  /** !!!Удлаяет все ответы пользователей */
  def removeProblemFromTemplateAndUpdateCourses(courseAlias: String, problemAlias: String): TranzactIO[Boolean] =
    for {
      res <- CourseTemplateProblemDao.removeProblemFromTemplate(courseAlias, problemAlias)
      courses <- CourseDao.linkedToTemplateCourses(courseAlias)
      _ <- ZIO.foreach(courses)(course => problemService.removeProblem(course.id, problemAlias)) //todo add problem service as dependency
    } yield res

  def getViewData(courseAlias: String): TranzactIO[Option[viewData.CourseTemplateViewData]] =
    for {
      ct <- DbCourseTemplateDao.byAliasOption(courseAlias)
      os <- templateProblemAliases(courseAlias)
    } yield ct.map(ct => viewData.CourseTemplateViewData(courseAlias, otsbridge.CoursePiece.fromJson(ct.courseData).title,
      ct.description, otsbridge.CoursePiece.fromJson(ct.courseData), os.map(_.problemAlias), true))

  def getTeacherCourses(teacherId: Int): TranzactIO[Seq[viewData.ShortCourseTemplateViewData]] =
    for {
      ct <- DbCourseTemplateDao.all
      res <- ZIO.foreach(ct)(ct =>
        for {
          os <- templateProblemAliases(ct.alias)
        } yield viewData.ShortCourseTemplateViewData(ct.alias, otsbridge.CoursePiece.fromJson(ct.courseData).title, ct.description, os.map(_.problemAlias))
      )
    } yield res


  def updateCourse(courseAlias: String, description: Option[String], data: Option[CourseRoot]): TranzactIO[Boolean] =
    for {
      a <- ZIO.when(description.nonEmpty)(DbCourseTemplateDao.setDescription(courseAlias, description.get))
      b <- ZIO.when(data.nonEmpty)(DbCourseTemplateDao.setCourseRoot(courseAlias, data.get))
    } yield (a.nonEmpty == description.nonEmpty) && (b.nonEmpty == data.nonEmpty)

  def registerTemplate(ct: otsbridge.CourseTemplate): UIO[Unit] =
    registryImpl.registerCourseTemplate(ct)
      .tap(_ => ZIO.log(s"Registering course template ${ct.uniqueAlias} ${ct.courseTitle} (${ct.problemAliasesToGenerate.size})"))
}

object CourseTemplateServiceImpl {
  def live: URIO[MessageBus & CourseTemplateRegistry & ProblemService, CourseTemplateService] =
    for {
      bus <- ZIO.service[MessageBus]
      reg <- ZIO.service[CourseTemplateRegistry]
      ver <- ZIO.service[ProblemService]
    } yield CourseTemplateServiceImpl(bus, reg, ver)

  def layer: URLayer[MessageBus & CourseTemplateRegistry & ProblemService, CourseTemplateService] =
    ZLayer.fromZIO(live)
}