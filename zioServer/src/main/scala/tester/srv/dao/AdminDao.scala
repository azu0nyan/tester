package tester.srv.dao


import io.github.gaelrenoux.tranzactio.doobie.{TranzactIO, tzio}
import AdminDao.Admin
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import AbstractDao.ById

object AdminDao extends AbstractDao [Admin] {

  case class Admin(userId: Int)

  override val schema: Schema[Admin] = DeriveSchema.gen[Admin]
  override val tableName: String = "Admin"

  def deleteById(userId: Int): TranzactIO[Boolean] = deleteWhere(fr"userId = $userId").map(_ == 1)

}

