package tester.srv.dao

import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import CourseDao.Course
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import AbstractDao.ById

import java.time.Instant


object CourseDao extends AbstractDao[Course]
  with ById[Course] {
  case class Course(id: Int, userId: Int, templateAlias: String, seed: Int, startedAt: Option[Instant], endedAt: Option[Instant])

  override val schema: Schema[Course] = DeriveSchema.gen[Course]
  override val tableName: String = "Course"

  def byAliasAndUserId(alias: String, userId: Int): TranzactIO[Option[Course]] =
    selectWhereAndOption(fr"userId = $userId", fr"templateAlias = $alias")

  def activeUserCourses(userId: Int): TranzactIO[List[Course]] =
    selectWhereList(fr"""userID = $userId AND (startedAt = NULL OR startedAt < NOW()::TIMESTAMP) AND
         (endedAt = NULL OR NOW()::TIMESTAMP < endedAt)""")
  

  def previousUserCourses(userId: Int): TranzactIO[List[Course]] =
    selectWhereList(fr"""userID = $userId AND (endedAt != NULL AND endedAt < NOW()::TIMESTAMP)""")

  def futureUserCourses(userId: Int): TranzactIO[List[Course]] =
    selectWhereList(fr"""userID = $userId AND (startedAt != NULL AND  NOW()::TIMESTAMP < statedAt)""")
  
  def linkedToTemplateCourses(templateAlias: String): TranzactIO[Seq[Course]] =
    selectWhereList(fr"templateAlias = $templateAlias")

}
