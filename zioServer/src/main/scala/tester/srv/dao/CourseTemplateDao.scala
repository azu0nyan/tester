package tester.srv.dao

import AbstractDao.*
import CourseTemplateDao.CourseTemplate
import doobie.implicits.*
import otsbridge.CoursePiece.CourseRoot
import zio.schema.{DeriveSchema, Schema}

object CourseTemplateDao extends AbstractDao[CourseTemplate]
  with ByAlias[CourseTemplate] {

  case class CourseTemplate(alias: String, description: String, courseData: String)
  
  override val schema: Schema[CourseTemplate] = DeriveSchema.gen[CourseTemplate]
  override val tableName: String = "CourseTemplate"
  override def jsonFields: Seq[String] = Seq("courseData")

  def setDescription(alias: String, description: String): TranzactIO[Boolean] =
    updateByAlias(alias, fr"description=$description")

  def setCourseRoot(alias: String, courseRoot: CourseRoot): AbstractDao[Boolean] =
    updateByAlias(alias, fr"courseData=${courseRoot.toJson}::json")

}

