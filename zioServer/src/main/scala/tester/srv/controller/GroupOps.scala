package tester.srv.controller

import zio.*
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import io.github.gaelrenoux.tranzactio.{DbException, doobie}
import doobie.{Connection, Database, TranzactIO, tzio}


object GroupOps {

  case class Group(id: Long, title: String, description: String)

  def createGroup(g: Group) = tzio {
    Update[Group](s"""INSERT INTO usergroup (id, title, description) VALUES (?, ?, ?)""")
      .updateMany(List(g))
  }

  def addUserToGroup(userId: Long, groupId: Long) = ???

  private def addUserToGroupQuery(userId: Long, groupId: Long) = tzio {
    sql"""INSERT INTO UserToGroup (userId, groupId, enteredAt)
         VALUES (${userId}, ${groupId}, ${java.time.Clock.systemUTC().instant()})""".update.run
  }

  def removeUserFromGroup(userId: Long, groupId: Long) = ???

  private def removeUserFromGroupQuery(userId: Long, groupId: Long) = tzio {
    sql"""UPDATE UserToGroup
         SET leavedat = ${java.time.Clock.systemUTC().instant()}
         WHERE userId = $userId AND groupId = $groupId""".update.run
  }

  def listGroups(): TranzactIO[List[Group]] = tzio {
    sql"""SELECT (id, title, description) FROM UserGroup""".query[Group].to[List]
  }

  def activeUserGroups(userId: Long): TranzactIO[List[Group]] = tzio {
    sql"""SELECT (id, title, description) FROM UserGroup
         WHERE enteredAt < NOW()::TIMESTAMP AND (leavedAt = NULL OR leavedAt < NOW::TIMESTAMP)
       """.query[Group].to[List]
  }


}
