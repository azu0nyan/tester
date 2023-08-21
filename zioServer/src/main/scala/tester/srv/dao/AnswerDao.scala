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

  def rejectedAnswers(problemId: Int): TranzactIO[List[Answer]] =
    (selectFragment ++
      fr"""INNER JOIN AnswerRejection as R ON R.answerId = id
          WHERE problemId = $problemID""").query[Answer].to[List]

  def verifiedAnswers(problemId: Int): TranzactIO[List[Answer]] =
    (selectFragment ++
      fr"""INNER JOIN AnswerVerification V ON V.answerId = id
          WHERE problemId = $problemID""").query[Answer].to[List]

  def confirmedAnswers(problemId: Int): TranzactIO[List[Answer]] =
    (selectFragment ++
      fr"""INNER JOIN AnswerVerificationConfirmation V ON V.answerId = id
          WHERE problemId = $problemID""").query[Answer].to[List]


  /** Успешно оцененные ответы, ожидающие подтверждения вручную */
  def unconfirmedAnswers(problemId: Int): TranzactIO[List[Answer]] =
    (selectFragment ++
      fr"""INNER JOIN AnswerVerification R ON R.answerId = id
           LEFT JOIN AnswerVerificationConfirmation C ON C.answerId = id
            WHERE problemId = $problemID 
            AND C.answerId IS NULL""").query[Answer].to[List]


  /**Ответы о результатох проверки которых нет информации в бд*/  
  def unverifiedAnswers(problemId: Int): TranzactIO[List[Answer]] =
    (selectFragment ++
      fr"""LEFT JOIN AnswerRejection R ON R.answerId = id
           LEFT JOIN AnswerVerification V ON V.answerId = id
            WHERE problemId = $problemID 
            AND R.answerId IS NULL AND V.answerId IS NULL""").query[Answer].to[List]

  /**Ответы котрорые проверились или еще проверяются */
  def currentUnrejectedAnswerCount(problemId: Int): TranzactIO[Int] =
    (sql"""SELECT Count(id) FROM $tableName
            LEFT JOIN AnswerRejection R ON R.answerId = id
            WHERE problemId = $problemID 
            AND R.answerId IS NULL""").query[Int].single


}
  
