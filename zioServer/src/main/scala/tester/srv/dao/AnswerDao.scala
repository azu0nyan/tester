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
import AnswerDao.Answer

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

  def rejectedAnswers(problemId: Int): TranzactIO[List[Answer]] = tzio {
    (selectFragment ++
      fr"""INNER JOIN AnswerRejection as R ON R.answerId = id
          WHERE problemId = $problemId""").query[Answer].to[List]
  }

  def verifiedAnswers(problemId: Int): TranzactIO[List[Answer]] = tzio {
    (selectFragment ++
      fr"""INNER JOIN AnswerVerification as V ON V.answerId = id
          WHERE problemId = $problemId""").query[Answer].to[List]
  }

  def confirmedAnswers(problemId: Int): TranzactIO[List[Answer]] = tzio {
    (selectFragment ++
      fr"""INNER JOIN AnswerVerificationConfirmation as V ON V.answerId = id
          WHERE problemId = $problemId""").query[Answer].to[List]
  }

  /** Успешно оцененные ответы, ожидающие подтверждения вручную */
  def unconfirmedAnswers(problemId: Option[Int], teacherId: Option[Int],
                         courseAlias: Option[String], groupId: Option[Int],
                         userId: Option[Int]): TranzactIO[List[Answer]] = tzio {
    (selectFragment ++
      fr"""INNER JOIN AnswerVerification R ON R.answerId = id
           LEFT JOIN AnswerVerificationConfirmation as Ver ON Ver.answerId = id
           LEFT JOIN Problem as P ON P.problemId = problemId
           LEFT JOIN Course ON P.courseId = Course.id
           LEFT JOIN CourseTemplate as CT ON CT.alias = Course.templateAlias
           LEFT JOIN RegisteredUser as U ON U.id = Course.userId
           LEFT JOIN UserToGroup as UTG ON UTG.userId = U.id
           LEFT JOIN UserGroup as G ON UTG.groupID = G.id
           LEFT JOIN TeacherToGroup as TTG ON TTG.groupId = G.id
           WHERE Ver.answerId IS NULL""" ++
      Fragments.whereAndOpt(
        problemId.map(pid => fr"problemId = $problemId"),
        teachedId.map(tid => fr"TTG.teacherID = $tid"),
        courseAlias.map(a => fr"CT.alias = $a"),
        groupId.map(gid => fr"G.id = $gid"),
        userId.map(uid => fr"U.id = $userId")
      )
      ).query[Answer].to[List]
  }


  /** Ответы о результатох проверки которых нет информации в бд */
  def unverifiedAnswers(problemId: Int): TranzactIO[List[Answer]] = tzio {
    (selectFragment ++
      fr"""LEFT JOIN AnswerRejection R ON R.answerId = id
           LEFT JOIN AnswerVerification V ON V.answerId = id
            WHERE problemId = $problemId
            AND R.answerId IS NULL AND V.answerId IS NULL""").query[Answer].to[List]
  }

  /** Ответы котрорые проверились или еще проверяются */
  def currentUnrejectedAnswerCount(problemId: Int): TranzactIO[Int] = tzio {
    (sql"""SELECT Count(id) FROM $tableName
            LEFT JOIN AnswerRejection R ON R.answerId = id
            WHERE problemId = $problemId
            AND R.answerId IS NULL""").query[Int].unique
  }


}
  
