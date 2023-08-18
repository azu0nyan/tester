package tester.srv.dao

import io.github.gaelrenoux.tranzactio.doobie.{TranzactIO, tzio}
import GroupDao.Group
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import AbstractDao.ById

object GroupDao extends AbstractDao [Group]
  with ById[Group]{

  case class Group(id: Int, title: String, description: String)

  override val schema: Schema[Group] = DeriveSchema.gen[Group]
  override val tableName: String = "Group"




}

