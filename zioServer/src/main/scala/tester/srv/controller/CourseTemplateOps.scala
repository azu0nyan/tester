package tester.srv.controller


import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
import tester.srv.dao.{AbstractDao, CourseDao, CourseTemplateProblemDao}

trait CourseTemplateOps[F[_]]{
  def addProblemToTemplateAndUpdateCourses(courseAlias: String, problemAlias: String):F[Boolean]

  def removeProblemFromTemplateAndUpdateCourses(courseAlias: String, problemAlias: String):F[Boolean]
}



