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
import tester.srv.dao.ProblemDao
import tester.srv.dao.ProblemDao.Problem

object ProblemOps {

  def startProblem(courseId: Long, templateAlias: String): TranzactIO[Int] = {
    val toInsert = Problem(0, courseId, templateAlias, scala.util.Random.nextLong(), "{}")
    ProblemDao.insert(toInsert)
  }

  def removeProblem(courseId: Long, templateAlias: String): TranzactIO[Unit] =
    for {
      problem <- ProblemDao.byCourseAndTemplate(courseId, templateAlias)
      _ <- ZIO.when(problem.nonEmpty)(ProblemDao.deleteById(problem.get.id))
    } yield ()


}
