package tester.srv.controller


import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
import tester.srv.dao.{AbstractDao, CourseDao, CourseTemplateProblemDao}

trait CourseTemplateService[F[_]]{

  def templateProblemAliases(alias: String): F[Seq[CourseTemplateProblem]]

  def createNewTemplate(alias: String, description: String):F[Boolean]

  def addProblemToTemplateAndUpdateCourses(courseAlias: String, problemAlias: String):F[Boolean]

  def removeProblemFromTemplateAndUpdateCourses(courseAlias: String, problemAlias: String):F[Boolean]
}



