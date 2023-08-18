package tester.srv.dao

import io.github.gaelrenoux.tranzactio.doobie.{TranzactIO, tzio}
import UserToGroupDao.UserToGroup
import zio.schema.{DeriveSchema, Schema}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import AbstractDao.ById
import java.time.Instant


object UserToGroupDao extends AbstractDao[UserToGroup]
  with ById[UserToGroup] {
  case class UserToGroup(id: Int, userId: Int, groupId: Int, enteredAt: Instant, leavedAt: Option[Instant])

  override val schema: Schema[UserToGroup] = DeriveSchema.gen[UserToGroup]
  override val tableName: String = "UserToGroup"


  val groupMembershipActive = fr"enteredAt < NOW()::TIMESTAMP AND (leavedAt = NULL OR leavedAt < NOW::TIMESTAMP)"

  def activeUserGroups(userId: Int): TranzactIO[List[UserToGroup]] =
    selectWhereAndList(groupMembershipActive, fr"userId = $userId")

  def usersInGroup(groupId: Int): TranzactIO[List[Int]] = tzio {
    sql"""SELECT userId FROM $tableName
           WHERE groupId = $groupId AND $groupMembershipActive""".query[Int].to[List]
  }
}


