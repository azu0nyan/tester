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

object ProblemOps {
  type Score = String //todo
  case class Problem(id: Long, courseId: Long, templateAlias: String, seed: Long, score: Score)

  val problemFields = fr"id, courseId, templateAlias, seed, score"
  val problemSelect = fr"SELECT $problemFields FROM Problem"

  def startProblem(courseId: Long, templateAlias: String) = tzio {
    val toInsert = Problem(0, courseId, templateAlias, scala.util.Random.nextLong(), "{}")
    Update[Problem](
      s"""INSERT INTO Problem ($problemFields)
         VALUES (?, ?, ?, ?, ?::jsonb)""").toUpdate0(toInsert).run
  }

  def problemByCourseAndTemplate(courseId: Long, templateAlias: String): TranzactIO[Option[Problem]] = tzio {
    (problemSelect ++ fr"""WHERE courseId = $courseId AND templateAlias = $templateAlias""")
      .query[Problem].option
  }

  def removeProblem(courseId: Long, templateAlias: String) =
    for {
      problem <- problemByCourseAndTemplate(courseId, templateAlias)
      _ <- ZIO.when(problem.nonEmpty)(removeProblemQuery(problem.get.id))
    } yield ()

  def removeProblemQuery(problemId: Long) = tzio {
    sql"""DELETE FROM Problem WHERE id = $problemId"""
      .update.run
  }
}
