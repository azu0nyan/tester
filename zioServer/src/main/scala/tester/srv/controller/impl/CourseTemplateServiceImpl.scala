package tester.srv.controller.impl

import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.controller.{CourseTemplateService, MessageBus, ProblemService}
import tester.srv.dao.CourseTemplateDao.CourseTemplate
import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
import tester.srv.dao.{CourseDao, CourseTemplateDao, CourseTemplateProblemDao}
import zio.*

case class CourseTemplateServiceImpl(
                                            bus: MessageBus,
                                            problemService: ProblemService
                                          ) extends CourseTemplateService {

  def templateProblemAliases(alias: String): TranzactIO[Seq[CourseTemplateProblem]] =
    CourseTemplateProblemDao.templateProblemAliases(alias)

  def createNewTemplate(alias: String, description: String): TranzactIO[Boolean] =
    CourseTemplateDao.insert(CourseTemplate(alias, description, "{}")) //todo insert default course data

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
}

object CourseTemplateServiceImpl {
  def live: URIO[MessageBus & ProblemService, CourseTemplateServiceImpl] =
    for {
      bus <- ZIO.service[MessageBus]
      ver <- ZIO.service[ProblemService]
    } yield CourseTemplateServiceImpl(bus, ver)

  def layer: URLayer[MessageBus & ProblemService, CourseTemplateServiceImpl] =
    ZLayer.fromZIO(live)
}