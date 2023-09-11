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
import AnswerVerificationConfirmationDao.AnswerVerificationConfirmation

import java.time.Instant

object AnswerVerificationConfirmationDao extends AbstractDao[AnswerVerificationConfirmation]{
  case class AnswerVerificationConfirmation(answerId: Int,  confirmedAt: Instant, confirmedById: Option[Int])

  override val schema: Schema[AnswerVerificationConfirmation] = DeriveSchema.gen[AnswerVerificationConfirmation]
  override val tableName: String = "AnswerVerificationConfirmation"


  def answerConfirmation(answerId: Int): TranzactIO[Option[AnswerVerificationConfirmation]] =
    selectWhereOption(fr"answerId=$answerId")
}


