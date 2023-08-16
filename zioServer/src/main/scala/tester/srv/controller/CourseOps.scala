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
import tester.srv.dao.{CourseDao, CourseTemplateDao, CourseTemplateProblemDao, UserSessionDao}

import java.time.Instant


/** Курс, который проходит ученик */
object CourseOps {

  /** Returns courseId */
  def startCourseForUser(alias: String, userId: Long): TranzactIO[Long] =
    for {
      courseTemplate <- CourseTemplateDao.byAliasOption(alias).map(_.get)
      course = CourseDao.Course(0, userId, courseTemplate.alias,
        scala.util.Random.nextLong(), Some(java.time.Clock.systemUTC().instant()), None)
      courseId <- CourseDao.insert(course)
      aliases <- CourseTemplateProblemDao.templateProblemAliases(courseTemplate.alias)
      _ <- ZIO.foreach(aliases)(a => ProblemOps.startProblem(courseId, a.problemAlias))
    } yield courseId

  /** Так же удаляет все ответы пользователя */
  def removeCourseFromUser(alias: String, userId: Long) =
    for {
      c <- CourseDao.byAliasAndUserId(alias, userId)
      _ <- ZIO.when(c.nonEmpty)(CourseDao.deleteById(c.get.id))
    } yield ()
}

