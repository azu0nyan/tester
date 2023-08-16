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

  private val courseFields = fr"id, userId, templateALias, seed, startedAt, endedAt"
  private val courseSelect = fr"SELECT $courseFields FROM course"

  def listCourses: TranzactIO[List[Course]] = tzio {
    courseSelect
      .query[Course].to[List]
  }

  def activeUserCourses(userId: Long): TranzactIO[List[Course]] = tzio {
    (courseSelect ++
      fr"""WHERE userID = $userId AND (startedAt = NULL OR startedAt < NOW()::TIMESTAMP) AND
           (endedAt = NULL OR NOW()::TIMESTAMP < endedAt)""")
      .query[Course].to[List]
  }

  def previousUserCourses(userId: Long): TranzactIO[List[Course]] = tzio {
    (courseSelect ++
      fr"""WHERE userID = $userId AND (endedAt != NULL AND endedAt < NOW()::TIMESTAMP)""")
      .query[Course].to[List]
  }

  def futureUserCourses(userId: Long): TranzactIO[List[Course]] = tzio {
    (courseSelect ++
      fr"""WHERE userID = $userId AND (startedAt != NULL AND  NOW()::TIMESTAMP < statedAt)""")
      .query[Course].to[List]
  }

  private def startCourseForUserQuery(c: Course): TranzactIO[Long] = tzio {
    Update[Course](
      s"""INSERT INTO Course ($courseFields)
         VALUES (?, ?, ?, ?, ?, ?)""").toUpdate0(c)
      .withUniqueGeneratedKeys("id")
  }

  def linkedToTemplateCourses(templateAlias: String): TranzactIO[Seq[Course]] = tzio {
    (courseSelect ++
      fr"""WHERE templateAlias = $templateAlias""")
      .query[Course].to[List]
  }

  /** Returns courseId */
  def startCourseForUser(alias: String, userId: Long): TranzactIO[Long] =
    for {
      courseTemplate <- CourseTemplateOps.templateByAlias(alias).map(_.get)
      course = Course(0, userId, courseTemplate.alias,
        scala.util.Random.nextLong(), Some(java.time.Clock.systemUTC().instant()), None)
      courseId <- startCourseForUserQuery(course)
      aliases <- CourseTemplateOps.templateProblemAliases(courseTemplate.alias)
      _ <- ZIO.foreach(aliases)(a => ProblemOps.startProblem(courseId, a))
    } yield courseId


    def removeCourseFromUser(alias: String, userId: Long) = ???


}
