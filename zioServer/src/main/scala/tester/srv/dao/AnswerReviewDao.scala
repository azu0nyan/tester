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
import AnswerReviewDao.AnswerReview

import java.time.Instant

object AnswerReviewDao extends AbstractDao[AnswerReview]{
  case class AnswerReview(answerId: Int,  text: String, reviewerId: Int)

  override val schema: Schema[AnswerReview] = DeriveSchema.gen[AnswerReview]
  override val tableName: String = "AnswerReview"
  
  def answerReview(answerId: Int) : TranzactIO[Option[AnswerReview]] =
    selectWhereOption(fr"answerId=$answerId")
}



