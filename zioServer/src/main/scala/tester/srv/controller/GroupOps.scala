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

  def addUserToGroup(userId: Long, groupId: Long) =
    for {
      _ <- addUserToGroupQuery(userId, groupId)
      courses <- forcedCourses(groupId)
      _ <- ZIO.foreach(courses)(course => CourseOps.startCourseForUser(course.templateAlias, userId))
    } yield ()

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

  val groupMembershipActive = fr"enteredAt < NOW()::TIMESTAMP AND (leavedAt = NULL OR leavedAt < NOW::TIMESTAMP)"

  def activeUserGroups(userId: Long): TranzactIO[List[Group]] = tzio {
    sql"""SELECT (id, title, description) FROM UserGroup
         WHERE $groupMembershipActive
       """.query[Group].to[List]
  }

  def usersInGroup(groupId: Long): TranzactIO[List[Long]] = tzio {
    sql"""SELECT userId FROM UserToGroup
         WHERE groupId = $groupId AND $groupMembershipActive""".query[Long].to[List]
  }

  case class CourseTemplateForGroup(id: Long, groupId: Long, templateAlias: String, forceStartForGroupMembers: Boolean)
  val courseTemplateForGroupFields = fr"id, groupId, templateAlias, forceStartForGroupMembers"
  val courseTemplateForGroupSelect = fr"SELECT $courseTemplateForGroupFields FROM CourseTemplateForGroup"

  /** Курсы, которые должны стартовать для всех участников группы автоматически */
  def forcedCourses(groupId: Long): TranzactIO[List[CourseTemplateForGroup]] = tzio {
    (courseTemplateForGroupSelect ++ fr"WHERE groupId = $groupId")
      .query[CourseTemplateForGroup].to[List]
  }

  private def addCourseToGroupQuery(templateAlias: String, groupId: Long, forceStart: Boolean) = tzio {
    sql"""INSERT INTO CourseTemplateForGroup ($courseTemplateForGroupFields)
         VALUES (0, $groupId, $templateAlias, $forceStart)
       """.update.run
  }

  private def removeCourseFromGroupQuery(templateAlias: String, groupId: Long) = tzio {
    sql"""DELETE FROM CourseTemplateForGroup WHERE
         templateAlias = $templateAlias AND groupId = $groupId""".update.run
  }

  def addCourseTemplateToGroup(templateAlias: String, groupId: Long, forceStart: Boolean) =
    for {
      _ <- addCourseToGroupQuery(templateAlias, groupId, forceStart)
      toStartUsersIds <-
        if (forceStart) usersInGroup(groupId)
        else ZIO.succeed(Seq())
      _ <- ZIO.foreach(toStartUsersIds)(userId => CourseOps.startCourseForUser(templateAlias, userId))
    } yield ()

  def removeCourseTemplateFromGroup(templateAlias: String, groupId: Long, forceRemoval: Boolean) =
    for {
      _ <- removeCourseFromGroupQuery(templateAlias, groupId)
      toRemoveUserIds <-
        if (forceRemoval) usersInGroup(groupId)
        else ZIO.succeed(Seq())
      _ <- ZIO.foreach(toRemoveUserIds)(userId => CourseOps.removeCourseFromUser(templateAlias, userId))
    } yield ()
}
