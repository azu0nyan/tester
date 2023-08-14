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
  def startProblem(courseId: Long, templateAlias: String) = tzio {
    val toInsert = Problem(0, courseId, templateAlias, scala.util.Random.nextLong(), "{}")
    Update(
      """INSERT INTO Problem (id, courseId, templateAlias, seed, score) 
         VALUES (?, ?, ?, ?, ?::jsonb)""").updateMany(List(toInsert))
  }
}
