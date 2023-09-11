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
import tester.srv.dao.AnswerRejectionDao.{AnswerRejection, answerRejection}
import tester.srv.dao.AnswerReviewDao.AnswerReview
import tester.srv.dao.AnswerVerificationConfirmationDao.AnswerVerificationConfirmation
import tester.srv.dao.AnswerVerificationDao.AnswerVerification
import zio.ZIO

import java.time.Instant


object AnswerDao extends AbstractDao[Answer]
  with ById[Answer] {
  case class Answer(id: Int, problemId: Int, answer: String, answeredAt: Instant)

  override val schema: Schema[Answer] = DeriveSchema.gen[Answer]
  override val tableName: String = "Answer"

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


  case class AnswerRejectionOpt(answerId: Option[Int], rejectedAt: Option[Instant], message: Option[String], rejectedBy: Option[Int])
  case class AnswerReviewOpt(answerId: Option[Int], text: Option[String], reviewerId: Option[Int])
  case class AnswerVerificationConfirmationOpt(answerId: Option[Int], confirmedAt: Option[Instant], confirmedById: Option[Int])
  case class AnswerVerificationOpt(answerId: Option[Int], verifiedAt: Option[Instant], systemMessage: Option[String], score: Option[String], scoreNormalized: Option[Double])


  def queryAnswers(filter: AnswerFilterParams)(andFrag: Option[Fragment] = None, addJoins: Fragment = fr""): TranzactIO[List[(Answer, AnswerMeta, AnswerStatusUnion)]] = tzio {

    val answerMetaFields = "U.id, Course.templateAlias, Course.id, P.id"
    val answerRejectionFields = "AnswerRejection.answerId, AnswerRejection.rejectedAt, AnswerRejection.message, AnswerRejection.rejectedBy"
    val answerReviewFields = "AnswerReview.answerId, AnswerReview.text, AnswerReview.reviewerId"
    val answerVerConfFields = "AnswerVerificationConfirmation.answerId, AnswerVerificationConfirmation.confirmedAt, AnswerVerificationConfirmation.confirmedById"
    val answerVerFields = "AnswerVerification.answerId, AnswerVerification.verifiedAt, AnswerVerification.systemMessage, AnswerVerification.score, AnswerVerification.scoreNormalized"
    val fields =
      fieldStringWithTable + ", " +
        answerMetaFields + ", " + answerRejectionFields + ", " + answerReviewFields + ", "+ answerVerConfFields + ", " + answerVerFields
//        AnswerRejectionDao.fieldStringWithTable + ", " +
//        AnswerReviewDao.fieldStringWithTable + ", " +
//        AnswerVerificationConfirmationDao.fieldStringWithTable + ", " +
//        AnswerVerificationDao.fieldStringWithTable

    val q = (fr"SELECT " ++ Fragment.const(fields + " FROM " + tableName) ++
      fr"""LEFT JOIN Problem as P ON P.id = problemId
         LEFT JOIN Course ON P.courseId = Course.id
         LEFT JOIN CourseTemplate as CT ON CT.alias = Course.templateAlias
         LEFT JOIN RegisteredUser as U ON U.id = Course.userId
         LEFT JOIN UserToGroup as UTG ON UTG.userId = U.id
         LEFT JOIN UserGroup as G ON UTG.groupID = G.id
         LEFT JOIN TeacherToGroup as TTG ON TTG.groupId = G.id
         LEFT JOIN AnswerVerification ON AnswerVerification.answerId = Answer.id
         LEFT JOIN AnswerVerificationConfirmation ON AnswerVerificationConfirmation.answerId = Answer.id
         LEFT JOIN AnswerReview ON AnswerReview.answerId = Answer.id
         LEFT JOIN AnswerRejection ON AnswerRejection.answerId = Answer.id
         """ ++
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


    q.query[(Answer, AnswerMeta, AnswerRejectionOpt, AnswerReviewOpt, AnswerVerificationConfirmationOpt, AnswerVerificationOpt)].to[List]
      .map(l => l.map { case (a, meta, rej, rev, conf, ver) =>
        (a, meta, AnswerStatusUnion(
        Option.when(ver.answerId.nonEmpty)(AnswerVerification(ver.answerId.get, ver.verifiedAt.get, ver.systemMessage, ver.score.get, ver.scoreNormalized.get)),
        Option.when(conf.answerId.nonEmpty)(AnswerVerificationConfirmation(conf.answerId.get, conf.confirmedAt.get, conf.confirmedById)),
        Option.when(rej.answerId.nonEmpty)(AnswerRejection(rej.answerId.get, rej.rejectedAt.get, rej.message, rej.rejectedBy)),
        Option.when(rev.answerId.nonEmpty)(AnswerReview(rev.answerId.get, rev.text.get, rev.reviewerId.get))
      ))
      })
  }

  /** Успешно оцененные ответы, ожидающие подтверждения вручную */
  def unconfirmedAnswers(filter: AnswerFilterParams): TranzactIO[List[(Answer, AnswerMeta, AnswerStatusUnion)]] =
    queryAnswers(filter)(Some(fr"answerverificationconfirmation.answerId IS NULL AND AnswerVerification.answerId IS NOT NULL AND AnswerVerification.scoreNormalized = 1.0"),
      fr"""""".stripMargin)


}
  
