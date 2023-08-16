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
  case class UserToGroup(id: Long, userId: Long, groupId: Long, enteredAt: Instant, leavedAt: Option[Instant])

  override val schema: Schema[UserToGroup] = DeriveSchema.gen[UserToGroup]
  override val tableName: String = "UserToGroup"


  val groupMembershipActive = fr"enteredAt < NOW()::TIMESTAMP AND (leavedAt = NULL OR leavedAt < NOW::TIMESTAMP)"

  def activeUserGroups(userId: Long): TranzactIO[List[UserToGroup]] =
    selectWhereAndList(groupMembershipActive, fr"userId = $userId")

  def usersInGroup(groupId: Long): TranzactIO[List[Long]] = tzio {
    sql"""SELECT userId FROM $tableName
           WHERE groupId = $groupId AND $groupMembershipActive""".query[Long].to[List]
  }
}


