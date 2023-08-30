package tester.srv.controller.impl

import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.controller.{Courses, ProblemService}
import tester.srv.dao.ProblemDao.Problem
import tester.srv.dao.{CourseDao, CourseTemplateDao, CourseTemplateProblemDao, ProblemDao}
import zio.ZIO

object CoursesTranzactIO extends Courses[TranzactIO] {

  /** Returns courseId */
  def startCourseForUser(alias: String, userId: Int): TranzactIO[Int] =
    for {
      courseTemplate <- CourseTemplateDao.byAliasOption(alias).map(_.get)
      course = CourseDao.Course(0, userId, courseTemplate.alias,
        scala.util.Random.nextInt(), Some(java.time.Clock.systemUTC().instant()), None)
      courseId <- CourseDao.insertReturnId(course)
      aliases <- CourseTemplateProblemDao.templateProblemAliases(courseTemplate.alias)
      _ <- ZIO.foreach(aliases)(a => ProblemServiceTranzactIO.startProblem(courseId, a.problemAlias))//todo add problem service as dependency
    } yield courseId


  def stopCourse(alias: String, userId: Int): TranzactIO[Unit] =
    for {
      c <- CourseDao.byAliasAndUserId(alias, userId)
      _ <- ZIO.when(c.nonEmpty)(CourseDao.setStopped(c.get.id))
    } yield ()

  /** Так же удаляет все ответы пользователя */
  def removeCourseFromUser(alias: String, userId: Int): TranzactIO[Unit] =
    for {
      c <- CourseDao.byAliasAndUserId(alias, userId)
      _ <- ZIO.when(c.nonEmpty)(CourseDao.deleteById(c.get.id))
    } yield ()


  def courseProblems(courseId: Int): TranzactIO[Seq[Problem]] =
    ProblemDao.courseProblems(courseId)
}
