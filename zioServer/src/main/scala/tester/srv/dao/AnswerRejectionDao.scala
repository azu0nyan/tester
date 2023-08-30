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
import AnswerRejectionDao.AnswerRejection

import java.time.Instant

object AnswerRejectionDao extends AbstractDao[AnswerRejection]{
  case class AnswerRejection(answerId: Int,  rejectedAt: Instant, message: Option[String], rejectedBy: Option[Int])

  override val schema: Schema[AnswerRejection] = DeriveSchema.gen[AnswerRejection]
  override val tableName: String = "AnswerRejection"

  def answerRejection(answerId: Int): TranzactIO[Option[AnswerRejection]] =
    selectWhereOption(fr"answerId=$answerId")
}


