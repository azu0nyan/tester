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
import AnswerVerificationDao.AnswerVerification
import otsbridge.ProgramRunResult.ProgramRunResult
import tester.srv.dao.ProblemDao.ScoreJsonString

import java.time.Instant

object AnswerVerificationDao extends AbstractDao[AnswerVerification]{
  case class AnswerVerification(answerId: Int, verifiedAt: Instant, systemMessage: Option[String], score: ScoreJsonString, scoreNormalized: Double)

//  DeriveSchema.gen[Seq[ProgramRunResult]]
  override val schema: Schema[AnswerVerification] = DeriveSchema.gen[AnswerVerification]
  override val tableName: String = "AnswerVerification"
  override val jsonbFields: Seq[String] = Seq("score")

  def answerVerification(answerId: Int): TranzactIO[Option[AnswerVerification]] =
    selectWhereOption(fr"answerId=$answerId")
}


