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


/** Курс, который проходит пользователь */
trait Courses[F[_]]{
  def startCourseForUser(alias: String, userId: Int): F[Int]

  def stopCourse(alias: String, userId: Int): F[Unit]

  /** Так же удаляет все ответы пользователя */
  def removeCourseFromUser(alias: String, userId: Int): F[Unit]
}

object Courses extends Courses[TranzactIO] {

  /** Returns courseId */
  def startCourseForUser(alias: String, userId: Int): TranzactIO[Int] =
    for {
      courseTemplate <- CourseTemplateDao.byAliasOption(alias).map(_.get)
      course = CourseDao.Course(0, userId, courseTemplate.alias,
        scala.util.Random.nextInt(), Some(java.time.Clock.systemUTC().instant()), None)
      courseId <- CourseDao.insertReturnId(course)
      aliases <- CourseTemplateProblemDao.templateProblemAliases(courseTemplate.alias)
      _ <- ZIO.foreach(aliases)(a => ProblemOps.startProblem(courseId, a.problemAlias))
    } yield courseId


  def stopCourse(alias: String, userId: Int): TranzactIO[Unit] =
    for{
      c <- CourseDao.byAliasAndUserId(alias, userId)
      _ <- ZIO.when(c.nonEmpty)(CourseDao.setStopped(c.get.id))
    } yield ()

  /** Так же удаляет все ответы пользователя */
  def removeCourseFromUser(alias: String, userId: Int):TranzactIO[Unit] =
    for {
      c <- CourseDao.byAliasAndUserId(alias, userId)
      _ <- ZIO.when(c.nonEmpty)(CourseDao.deleteById(c.get.id))
    } yield ()
}

