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

object TeacherToGroupDao extends AbstractDao [TeacherToGroup] {

  case class TeacherToGroup(userId: Int)

  override val schema: Schema[TeacherToGroup] = DeriveSchema.gen[TeacherToGroup]
  override val tableName: String = "TeacherToGroup"

}



