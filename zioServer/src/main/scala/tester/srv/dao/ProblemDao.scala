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
import otsbridge.ProblemScore.ProblemScore

import java.time.Instant


object ProblemDao extends AbstractDao[Problem]
  with ById[Problem] {

  type ScoreJsonString = String
  case class Problem(id: Int, courseId: Int, templateAlias: String, seed: Int,
                     score: ScoreJsonString, scoreNormalized: Double,
                     maxAttempts: Option[Int], deadline: Option[Instant], requireConfirmation: Boolean)

  override val schema: Schema[Problem] = DeriveSchema.gen[Problem]
  override val tableName: String = "Problem"
  override val jsonbFields: Seq[String] = Seq("score")

  def byCourseAndTemplate(courseId: Int, templateAlias: String): TranzactIO[Option[Problem]] =
    selectWhereAndOption(fr"courseId = $courseId", fr"templateAlias = $templateAlias")

  def courseProblems(courseId: Int): TranzactIO[List[Problem]] =
    selectWhereList(fr"courseId = $courseId")

}

