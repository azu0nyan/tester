package tester.srv.dao

import tester.srv.controller.CourseTemplateOps.CourseTemplate
import zio.schema.{DeriveSchema, Schema}

object CourseTemplateDao
  extends AbstractDao[CourseTemplate]
    with ByAlias[CourseTemplate] {
  override val schema: Schema[CourseTemplate] = DeriveSchema.gen[CourseTemplate]
  override val tableName: String = "CourseTemplate"
  override def jsonFields: Seq[String] = Seq("courseData")
}
