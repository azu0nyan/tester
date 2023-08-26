package tester.srv.dao


import io.github.gaelrenoux.tranzactio.doobie.{TranzactIO, tzio}
import TeacherCourseTemplateDao.TeacherCourseTemplate
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import AbstractDao.ById

object TeacherCourseTemplateDao extends AbstractDao [TeacherCourseTemplate] {

  case class TeacherCourseTemplate(teacherId: Int, courseTemplateAlias: String)

  override val schema: Schema[TeacherCourseTemplate] = DeriveSchema.gen[TeacherCourseTemplate]
  override val tableName: String = "TeacherCourseTemplate"
}


