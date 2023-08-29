package tester.srv.controller.impl


import zio.*
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import io.github.gaelrenoux.tranzactio.{DbException, doobie}
import doobie.{Connection, Database, TranzactIO, tzio}
import tester.srv.controller.ProblemService
import tester.srv.dao.ProblemDao
import tester.srv.dao.ProblemDao.Problem


object ProblemServiceTranzactIO extends ProblemService[TranzactIO]{

  def startProblem(courseId: Int, templateAlias: String): TranzactIO[Int] = {
    val toInsert = Problem(0, courseId, templateAlias, scala.util.Random.nextInt(), "{}", 0d,  None, None )
    ProblemDao.insertReturnId(toInsert)
  }

  def removeProblem(courseId: Int, templateAlias: String): TranzactIO[Unit] =
    for {
      problem <- ProblemDao.byCourseAndTemplate(courseId, templateAlias)
      _ <- ZIO.when(problem.nonEmpty)(ProblemDao.deleteById(problem.get.id))
    } yield ()


}

