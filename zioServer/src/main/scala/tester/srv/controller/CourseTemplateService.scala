package tester.srv.controller


import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
import tester.srv.dao.{AbstractDao, CourseDao, CourseTemplateProblemDao}
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import otsbridge.CoursePiece.CourseRoot
import otsbridge.CourseTemplate
import zio.*

trait CourseTemplateService{

  def templateProblemAliases(alias: String): TranzactIO[Seq[CourseTemplateProblem]]

  def createNewTemplate(alias: String, description: String): TranzactIO[Boolean]

  def addProblemToTemplateAndUpdateCourses(courseAlias: String, problemAlias: String): TranzactIO[Boolean]

  def removeProblemFromTemplateAndUpdateCourses(courseAlias: String, problemAlias: String): TranzactIO[Boolean]

  def getViewData(courseAlias: String): TranzactIO[Option[viewData.CourseTemplateViewData]]
  
  def getTeacherCourses(teacherId: Int): TranzactIO[Seq[viewData.ShortCourseTemplateViewData]]

  def updateCourse(courseAlias: String, description: Option[String], data: Option[CourseRoot]): TranzactIO[Boolean]
  
  def registerTemplate(ct: otsbridge.CourseTemplate): UIO[Unit]
}



