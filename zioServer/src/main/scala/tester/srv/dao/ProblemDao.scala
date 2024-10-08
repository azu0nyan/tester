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
import cats.syntax.all.catsSyntaxSeqs
import otsbridge.ProblemScore.ProblemScore
import utils.SQL

import java.time.Instant


object ProblemDao extends AbstractDao[Problem]
  with ById[Problem] {

  type ScoreJsonString = String
  case class Problem(id: Int, courseId: Int, templateAlias: String, seed: Int,
                     score: ScoreJsonString, scoreNormalized: Double,
                     maxAttempts: Option[Int], deadline: Option[Instant], requireConfirmation: Boolean, addedAt: Instant)

  override val schema: Schema[Problem] = DeriveSchema.gen[Problem]
  override val tableName: String = "Problem"
  override val jsonbFields: Seq[String] = Seq("score")

  def byCourseAndTemplate(courseId: Int, templateAlias: String): TranzactIO[Option[Problem]] =
    selectWhereAndOption(fr"courseId = $courseId", fr"templateAlias = $templateAlias")

  def courseProblems(courseId: Int): TranzactIO[List[Problem]] =
    selectWhereList(fr"courseId = $courseId")

  def updateScore(problemId: Int, score: ProblemScore): TranzactIO[Boolean] =
    updateById(problemId, fr"scoreNormalized=${score.percentage}, score=${score.toJson}::jsonb")

  sealed trait ProblemFilter
  object ProblemFilter {
    case class ByUsers(userId: Int*) extends ProblemFilter
    case class FromGroupCourses(groupId: Int) extends ProblemFilter
    case class ByCourses(courseId: Int*) extends ProblemFilter
    case class ByCourseAliases(courseId: String*) extends ProblemFilter
  }

  def filterToFragment(f: ProblemFilter): Option[Fragment] = f match
    case ProblemFilter.ByUsers(userIds*) =>
      userIds.toNeSeq.map(ids => Fragments.in(fr"C.userId", ids))
    case ProblemFilter.FromGroupCourses(groupId) =>
      None //todo user can have same courses from different groups, forbid that or add filter here
    case ProblemFilter.ByCourses(courseIds*) =>
      courseIds.toNeSeq.map(cids => Fragments.in(fr"P.courseId", cids))
    case ProblemFilter.ByCourseAliases(courseAliases*) =>
      courseAliases.toNeSeq.map(as => Fragments.in(fr"C.templateAlias", as))

  case class ProblemMeta(userId: Int, courseAlias: String, answers: Int, rejectedAnswers: Int, verifiedAnswers: Int, confirmed: Int, reviews: Int /*, confirmedNonRejected: Int*/)
  def queryProblems(filter: ProblemFilter*): TranzactIO[Seq[(Problem, ProblemMeta)]] = tzio {
    val q = Fragment.const(
      s"""SELECT ${fieldNames.map(n => s"P.$n").mkString(", ")}, C.userId, C.templateAlias,
         |       COUNT(A.id), COUNT(R.answerId), COUNT(V.answerId), COUNT(VC.answerId), COUNT(Rev.answerId)
         |FROM $tableName as P
         |LEFT JOIN ${CourseDao.tableName} as C ON C.id = P.courseId
         |LEFT JOIN ${AnswerDao.tableName} as A ON A.problemId = P.id
         |LEFT JOIN ${AnswerRejectionDao.tableName} as R on R.answerId = A.id
         |LEFT JOIN ${AnswerVerificationDao.tableName} as V on V.answerId = A.id
         |LEFT JOIN ${AnswerVerificationConfirmationDao.tableName} as VC on VC.answerId = A.id
         |LEFT JOIN ${AnswerReviewDao.tableName} as Rev on Rev.answerId = A.id
         |""".stripMargin) ++ SQL.whereAndOpt(filter.map(filterToFragment): _ *) ++
      fr"GROUP BY P.id, C.userId, C.templateAlias"
    q.query[(Problem, ProblemMeta)].to[List]
  }


}

