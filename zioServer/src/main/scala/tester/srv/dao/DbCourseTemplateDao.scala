package tester.srv.dao

import AbstractDao.*
import DbCourseTemplateDao.DbCourseTemplate
import doobie.implicits.*
import otsbridge.CoursePiece.CourseRoot
import zio.schema.{DeriveSchema, Schema}
import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import otsbridge.{CoursePiece, CourseTemplate}

object DbCourseTemplateDao extends AbstractDao[DbCourseTemplate]
  with ByAlias[DbCourseTemplate] {

  case class DbCourseTemplate(alias: String, description: String, courseData: String)

  def toCourseTemplate(db: DbCourseTemplate):CourseTemplate = new CourseTemplate{
    override def courseData: CourseRoot = CoursePiece.fromJson(db.courseData)
    override def description = db.description
    override val courseTitle: String = courseData.title
    override val uniqueAlias: String = db.alias
  }

  override val schema: Schema[DbCourseTemplate] = DeriveSchema.gen[DbCourseTemplate]
  override val tableName: String = "CourseTemplate"
  override def jsonFields: Seq[String] = Seq("courseData")

  def setDescription(alias: String, description: String): TranzactIO[Boolean] =
    updateByAlias(alias, fr"description=$description")

  def setCourseRoot(alias: String, courseRoot: CourseRoot): TranzactIO[Boolean] =
    updateByAlias(alias, fr"courseData=${courseRoot.toJson}::json")



}

