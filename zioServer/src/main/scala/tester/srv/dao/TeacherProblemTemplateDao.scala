package tester.srv.dao

import io.github.gaelrenoux.tranzactio.doobie.{TranzactIO, tzio}
import TeacherProblemTemplateDao.TeacherProblemTemplate
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import AbstractDao.ById

object TeacherProblemTemplateDao extends AbstractDao [TeacherProblemTemplate] {

  case class TeacherProblemTemplate(teacherId: Int, courseTemplateAlias: String)

  override val schema: Schema[TeacherProblemTemplate] = DeriveSchema.gen[TeacherProblemTemplate]
  override val tableName: String = "TeacherCourseTemplate"
}



