package tester.srv.dao


import io.github.gaelrenoux.tranzactio.doobie.{TranzactIO, tzio}
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import AbstractDao.*
import ProblemDao.Problem

import java.time.Instant


object ProblemDao extends AbstractDao[Problem]
  with ById[Problem] {

  type Score = String //todo
  case class Problem(id: Long, courseId: Long, templateAlias: String, seed: Long, score: Score)

  override val schema: Schema[Problem] = DeriveSchema.gen[Problem]
  override val tableName: String = "Problem"
  override val jsonbFields: Seq[String] = Seq("score")

  def byCourseAndTemplate(courseId: Long, templateAlias: String): TranzactIO[Option[Problem]] =
    selectWhereAndOption(fr"courseId = $courseId", fr"templateAlias = $templateAlias")

}

