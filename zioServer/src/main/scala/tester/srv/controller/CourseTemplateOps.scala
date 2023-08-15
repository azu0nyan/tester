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
  case class CourseTemplate(alias: String, description: String, courseData: String)

  def templateByAlias(alias: String) = tzio {
    sql"""SELECT templateAlias, description, courseData FROM CourseTemplate
         WHERE templateAlias = $alias
       """.query[CourseTemplate].option
  }

  def templateProblemAliases(alias: String): TranzactIO[Seq[String]] = tzio {
    sql"""SELECT problemAlias FROM CourseTemplateProblemAlias
         JOIN CourseTemplate ON
         CourseTemplateProblemAlias.courseId = CourseTemplate.Id
         WHERE templateAlias = $alias
       """.query[String].to[List]
  }


  def insertCourseTemplateProblem(courseAlias: String, problemAlias: String) = tzio {
    sql"""INSERT INTO CourseTemplateProblemAlias
         (courseAlias, problemAlias) VALUES ($courseAlias, $problemAlias)"""
      .update.run
  }

  def addProblemToTemplateAndUpdateCourses(courseAlias: String, problemAlias: String) =
    for {
      _ <- insertCourseTemplateProblem(courseAlias, problemAlias)
      courses <- CourseOps.linkedToTemplateCourses(courseAlias)
      _ <- ZIO.foreach(courses)(course => ProblemOps.startProblem(course.id, problemAlias))
    } yield ()

 
  private def removeProblemFromTemplateQuery(courseAlias: String, problemAlias: String) = tzio{
    sql"""DELETE FROM CourseTemplateProblemAlias 
         WHERE courseALias = $courseAlias AND problemAlias = $problemAlias"""
      .update.run
  }

  /**!!!Удлаяет все ответы пользователей*/
  def removeProblemFromTemplateAndUpdateCourses(courseAlias: String, problemAlias: String) =
    for{
      _ <- removeProblemFromTemplateQuery(courseAlias, problemAlias)
      courses <- CourseOps.linkedToTemplateCourses(courseAlias)
      _ <- ZIO.foreach(courses)(course => ProblemOps.removeProblem(course.id, problemAlias))
    } yield ()
}
