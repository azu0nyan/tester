package tester.srv.dao


import io.github.gaelrenoux.tranzactio.doobie.{TranzactIO, tzio}
import TeacherToGroupDao.TeacherToGroup
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import AbstractDao.ById

object TeacherToGroupDao extends AbstractDao[TeacherToGroup] {

  case class TeacherToGroup(teacherId: Int, groupId: Int)

  override val schema: Schema[TeacherToGroup] = DeriveSchema.gen[TeacherToGroup]
  override val tableName: String = "TeacherToGroup"

  def teacherGroups(teacherId: Int): TranzactIO[Seq[TeacherToGroup]] =
    selectWhereList(fr"teacherId=$teacherId")

  def groupTeachers(groupId: Int): TranzactIO[Seq[TeacherToGroup]] =
    selectWhereList(fr"groupId=$groupId")

}



