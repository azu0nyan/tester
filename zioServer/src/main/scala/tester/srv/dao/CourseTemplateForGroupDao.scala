package tester.srv.dao


import io.github.gaelrenoux.tranzactio.doobie.{TranzactIO, tzio}
import CourseTemplateForGroupDao.CourseTemplateForGroup
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*


object CourseTemplateForGroupDao extends AbstractDao[CourseTemplateForGroup] {
  case class CourseTemplateForGroup(id: Int, groupId: Int, templateAlias: String, forceStartForGroupMembers: Boolean)

  override val schema: Schema[CourseTemplateForGroup] = DeriveSchema.gen[CourseTemplateForGroup]
  override val tableName: String = "CourseTemplateForGroup"

  def removeCourseFromGroup(templateAlias: String, groupId: Int): TranzactIO[Int] =
    deleteWhere(fr"templateAlias = $templateAlias AND groupId = $groupId")

  /** Курсы, которые должны стартовать для всех участников группы автоматически */
  def forcedCourses(groupId: Int): TranzactIO[List[CourseTemplateForGroup]] =
    selectWhereList(fr"groupId = $groupId")

}

