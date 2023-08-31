package tester.srv.controller


import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
import tester.srv.dao.{AbstractDao, CourseDao, CourseTemplateProblemDao}
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO

trait CourseTemplateService{

  def templateProblemAliases(alias: String): TranzactIO[Seq[CourseTemplateProblem]]

  def createNewTemplate(alias: String, description: String): TranzactIO[Boolean]

  def addProblemToTemplateAndUpdateCourses(courseAlias: String, problemAlias: String): TranzactIO[Boolean]

  def removeProblemFromTemplateAndUpdateCourses(courseAlias: String, problemAlias: String): TranzactIO[Boolean]
}



