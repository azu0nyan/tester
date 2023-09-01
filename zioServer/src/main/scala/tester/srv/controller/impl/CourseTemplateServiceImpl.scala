package tester.srv.controller.impl

import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import otsbridge.CoursePiece.CourseRoot
import tester.srv.controller.{CourseTemplateRegistry, CourseTemplateService, MessageBus, ProblemService}
import tester.srv.dao.CourseTemplateDao.CourseTemplate
import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
import tester.srv.dao.{CourseDao, CourseTemplateDao, CourseTemplateProblemDao}
import zio.*

case class CourseTemplateServiceImpl(
                                      bus: MessageBus,
                                      registryImpl: CourseTemplateRegistry,
                                      problemService: ProblemService
                                    ) extends CourseTemplateService {

  def templateProblemAliases(alias: String): TranzactIO[Seq[CourseTemplateProblem]] =
    CourseTemplateProblemDao.templateProblemAliases(alias)

  def createNewTemplate(alias: String, description: String): TranzactIO[Boolean] =
    CourseTemplateDao.insert(CourseTemplate(alias, description, otsbridge.CoursePiece.CourseRoot(alias, "", Seq()).toJson))

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

  def getViewData(courseAlias: String): TranzactIO[viewData.CourseTemplateViewData] =
    for{
      ct <- CourseTemplateDao.byAlias(courseAlias)
      os <- templateProblemAliases(courseAlias)
    }  yield viewData.CourseTemplateViewData(courseAlias, otsbridge.CoursePiece.fromJson(ct.courseData).title, ct.description, os.map(_.problemAlias))


  def updateCourse(courseAlias: String, description: Option[String], data: Option[CourseRoot]): TranzactIO[Boolean] =
    for{
      a <- ZIO.when(description.nonEmpty)(CourseTemplateDao.setDescription(description.get))
      b <- ZIO.when(data.nonEmpty)(CourseTemplateDao.setCourseRoot(data.ge))
    } yield (a.nonEmpty == description.nonEmpty) && (b.nonEmpty == data.nonEmpty)
    
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