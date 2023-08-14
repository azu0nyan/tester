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

import java.time.Instant


/** Курс, который проходит ученик */
object CourseOps {

  case class Course(id: Long, userId: Long, templateAlias: String, seed: Long, startedAt: Option[Instant], endedAt: Option[Instant])

  def listCourses: TranzactIO[List[Course]] = tzio {
    sql"""SELECT (id, userId, templateALias, seed, startedAt, endedAt) FROM course"""
      .query[Course].to[List]
  }

  def activeUserCourses: TranzactIO[List[Course]] = tzio {
    sql"""SELECT (id, userId, templateAlias, seed, startedAt, endedAt) FROM course
         WHERE (startedAt = NULL OR startedAt < NOW()::TIMESTAMP) AND
           (endedAt = NULL OR NOW()::TIMESTAMP < endedAt)
       """
      .query[Course].to[List]
  }

  def previousUserCourses: TranzactIO[List[Course]] = tzio {
    sql"""SELECT (id, userId, templateAlias, seed, startedAt, endedAt) FROM course
         WHERE (endedAt != NULL AND endedAt < NOW()::TIMESTAMP)
       """
      .query[Course].to[List]
  }

  def futureUserCourses: TranzactIO[List[Course]] = tzio {
    sql"""SELECT (id, userId, templateAlias, seed, startedAt, endedAt) FROM course
         WHERE (startedAt != NULL AND  NOW()::TIMESTAMP < statedAt)
       """
      .query[Course].to[List]

  }

  private def startCourseForUserQuery(c: Course) = tzio {
    Update[Course](
      s"""INSERT INTO Course (id, userID, templateAlias, seed, startedAt, endedAt)
         VALUES (?, ?, ?, ?, ?, ?)""").updateMany(List(c))
  }

  def startCourseForUser(alias: String, userId: Long) =
    for{
      courseOpt <- CourseTemplateOps.templateByAlias(alias)
      course = courseOpt.get
      aliases <- CourseTemplateOps.problemAliases(course.id)
      _ <- aliases.foreach(startProblem(...))
    } yield ()

}
