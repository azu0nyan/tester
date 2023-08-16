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
import tester.srv.dao.CourseTemplateProblemDao.CourseTemplateProblem
import tester.srv.dao.{AbstractDao, CourseTemplateProblemDao}


object CourseTemplateOps {



//  def insertCourseTemplateProblem(courseAlias: String, problemAlias: String) = tzio {
//    sql"""INSERT INTO CourseTemplateProblem
//         (courseAlias, problemAlias) VALUES ($courseAlias, $problemAlias)"""
//      .update.run
//  }

  def addProblemToTemplateAndUpdateCourses(courseAlias: String, problemAlias: String) =
    for {
      _ <- CourseTemplateProblemDao.insert(CourseTemplateProblem(courseAlias, problemAlias))
      courses <- CourseDao.linkedToTemplateCourses(courseAlias)
      _ <- ZIO.foreach(courses)(course => ProblemOps.startProblem(course.id, problemAlias))
    } yield ()


  private def removeProblemFromTemplateQuery(courseAlias: String, problemAlias: String) = tzio {
    sql"""DELETE FROM CourseTemplateProblem
         WHERE courseAlias = $courseAlias AND problemAlias = $problemAlias"""
      .update.run
  }

  /** !!!Удлаяет все ответы пользователей */
  def removeProblemFromTemplateAndUpdateCourses(courseAlias: String, problemAlias: String) =
    for {
      _ <- removeProblemFromTemplateQuery(courseAlias, problemAlias)
      courses <- CourseDao.linkedToTemplateCourses(courseAlias)
      _ <- ZIO.foreach(courses)(course => ProblemOps.removeProblem(course.id, problemAlias))
    } yield ()
}



