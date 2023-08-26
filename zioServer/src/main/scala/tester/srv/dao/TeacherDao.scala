package tester.srv.dao


import io.github.gaelrenoux.tranzactio.doobie.{TranzactIO, tzio}
import TeacherDao.Teacher
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import AbstractDao.ById

object TeacherDao extends AbstractDao [Teacher] {

  case class Teacher(userId: Int)

  override val schema: Schema[Teacher] = DeriveSchema.gen[Teacher]
  override val tableName: String = "Teacher"

}


