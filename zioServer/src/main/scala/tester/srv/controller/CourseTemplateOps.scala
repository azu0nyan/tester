package tester.srv.controller

import zio.*
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import io.github.gaelrenoux.tranzactio.{DbException, doobie}
import doobie.{Connection, Database, TranzactIO, tzio}


object CourseTemplateOps {
  case class CustomCourseTemplate(id: Long, templateAlias: String, description: String, courseData: String)

  def templateByAlias(alias: String) = tzio{
    sql"""SELECT id, templateAlias, description, courseData FROM CustomCourseTemplate
         WHERE templateAlias = alias
       """.query[CustomCourseTemplate].option
  }

  def problemAliases(courseId: Long): TranzactIO[Seq[String]] = tzio {
    sql"""SELECT problemAlias FROM CustomCourseTemplateProblemAlias
         JOIN CustomCourseTemplate ON
         CustomCourseTemplateProblemAlias.courseId =  CustomCourseTemplate.Id
         WHERE CustomCourseTemplate.Id = courseID
       """.query[String].to[List]
  }

  def addProblemToCourse(courseId: Long, problemAlias: String) = ???

  def removeProblemFromCourse(courseId: Long, problemAlias: String) = ???
}
