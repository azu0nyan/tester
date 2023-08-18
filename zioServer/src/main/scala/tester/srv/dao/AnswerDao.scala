package tester.srv.dao

package tester.srv.dao


import io.github.gaelrenoux.tranzactio.doobie.{TranzactIO, tzio}
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import AbstractDao.ById
import tester.srv.dao.AnswerDao.Answer

import java.time.Instant


object AnswerDao extends AbstractDao[Answer]
  with ById[Answer] {
  case class Answer(id: Int, problemId: Int, answer: String, status: String, answeredAt: Instant)

  override val schema: Schema[Answer] = DeriveSchema.gen[Answer]
  override val tableName: String = "Problem"
  override val jsonbFields: Seq[String] = Seq("status")


  def problemAnswers(problemId: Int): TranzactIO[List[Answer]] =
    selectWhereList(fr"WHERE problemId = $problemId")

  def deleteProblemAnswers(problemId: Int): TranzactIO[Int] =
    deleteWhere(fr"problemId = $problemId")

}
  
