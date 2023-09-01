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
import tester.srv.controller.AnswerService.{AnswerFilterParams, AnswerStatusUnion}

import java.time.Instant


object AnswerDao extends AbstractDao[Answer]
  with ById[Answer] {
  case class Answer(id: Int, problemId: Int, answer: String, status: String, answeredAt: Instant)

  override val schema: Schema[Answer] = DeriveSchema.gen[Answer]
  override val tableName: String = "Answer"
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

  case class AnswerMeta(userId: Int, courseAlias: String, courseId: Int, problemId: Int)
  val answerMetaFields = "U.id, Course.templateAlias, Course.id, P.id"
  //todo correct status
  def queryAnswers(filter: AnswerFilterParams)(andFrag: Option[Fragment] = None, addJoins: Fragment = fr""): TranzactIO[List[(Answer, AnswerMeta, AnswerStatusUnion)]] = tzio {
    val q = (fr"SELECT " ++ Fragment.const(fieldStringWithTable + ", " + answerMetaFields + " FROM " + tableName) ++
      fr"""LEFT JOIN Problem as P ON P.id = problemId
         LEFT JOIN Course ON P.courseId = Course.id
         LEFT JOIN CourseTemplate as CT ON CT.alias = Course.templateAlias
         LEFT JOIN RegisteredUser as U ON U.id = Course.userId
         LEFT JOIN UserToGroup as UTG ON UTG.userId = U.id
         LEFT JOIN UserGroup as G ON UTG.groupID = G.id
         LEFT JOIN TeacherToGroup as TTG ON TTG.groupId = G.id""" ++
      addJoins ++
      fr"""WHERE """ ++
      Fragments.andOpt(
        filter.answerId.map(aid => fr"Answer.id = $aid"),
        filter.problemId.map(pid => fr"problemId = $pid"),
        filter.problemAlias.map(a => fr"P.templateAlias = $a"),
        filter.teacherId.map(tid => fr"TTG.teacherID = $tid"),
        filter.courseAlias.map(a => fr"CT.alias = $a"),
        filter.groupId.map(gid => fr"G.id = $gid"),
        filter.userId.map(uid => fr"U.id = $uid"),
        andFrag
      )
      )
      println(q)
      q.query[(Answer, AnswerMeta)].to[List].map(l => l.map { case (a, b) => (a, b, AnswerStatusUnion(None, None, None, None)) })
  }

  /** Успешно оцененные ответы, ожидающие подтверждения вручную */
  def unconfirmedAnswers(filter: AnswerFilterParams): TranzactIO[List[(Answer, AnswerMeta, AnswerStatusUnion)]] =
    queryAnswers(filter)(Some(fr"Conf.answerId IS NULL AND VER.answerId IS NOT NULL AND VER.scoreNormalized = 1.0"),
      fr"""LEFT JOIN AnswerVerification VER ON VER.answerId = Answer.id
          |LEFT JOIN AnswerVerifiactionConfirmation as Conf ON Conf.answerId = Answer.id""".stripMargin)


}
  
