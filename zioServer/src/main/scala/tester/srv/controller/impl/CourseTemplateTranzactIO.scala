package tester.srv.controller.impl

import doobie.util.transactor
import io.github.gaelrenoux.tranzactio.DbException
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.controller.{CourseTemplateOps, ProblemService}
import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
import tester.srv.dao.{CourseDao, CourseTemplateProblemDao}
import zio.{Task, ZIO}

object CourseTemplateTranzactIO extends CourseTemplateOps[TranzactIO] {

  def addProblemToTemplateAndUpdateCourses(courseAlias: String, problemAlias: String): TranzactIO[Boolean] =
    for {
      res <- CourseTemplateProblemDao.insert(CourseTemplateProblem(courseAlias, problemAlias))
      courses <- CourseDao.linkedToTemplateCourses(courseAlias)
      _ <- ZIO.foreach(courses)(course => ProblemServiceTranzactIO.startProblem(course.id, problemAlias)) //todo add problem service as dependency
    } yield res


  /** !!!Удлаяет все ответы пользователей */
  def removeProblemFromTemplateAndUpdateCourses(courseAlias: String, problemAlias: String): TranzactIO[Boolean] =
    for {
      res <- CourseTemplateProblemDao.removeProblemFromTemplate(courseAlias, problemAlias)
      courses <- CourseDao.linkedToTemplateCourses(courseAlias)
      _ <- ZIO.foreach(courses)(course => ProblemServiceTranzactIO.removeProblem(course.id, problemAlias))//todo add problem service as dependency
    } yield res
}
