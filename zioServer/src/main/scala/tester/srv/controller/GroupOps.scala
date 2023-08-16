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
import tester.srv.dao.CourseTemplateForGroupDao.CourseTemplateForGroup
import tester.srv.dao.{CourseTemplateForGroupDao, UserToGroupDao}
import tester.srv.dao.UserToGroupDao.UserToGroup
import tester.srv.dao.UserToGroupDao.UserToGroup


object GroupOps {


  def addUserToGroup(userId: Long, groupId: Long) =
    for {
      _ <- addUserToGroupQuery(userId, groupId)
      courses <- CourseTemplateForGroupDao.forcedCourses(groupId)
      _ <- ZIO.foreach(courses)(course => CourseOps.startCourseForUser(course.templateAlias, userId))
    } yield ()

  private def addUserToGroupQuery(userId: Long, groupId: Long) =
    UserToGroupDao.insert(UserToGroup(0, userId, groupId, java.time.Clock.systemUTC().instant(), None))


  def removeUserFromGroup(userId: Long, groupId: Long) = ???

  private def removeUserFromGroupQuery(userId: Long, groupId: Long) =
    UserToGroupDao.updateWhere(fr"leavedat = ${java.time.Clock.systemUTC().instant()} ",
      fr"userId = $userId AND groupId = $groupId")


  def addCourseToGroupQuery(templateAlias: String, groupId: Long, forceStart: Boolean) =
    CourseTemplateForGroupDao.insert(CourseTemplateForGroup(0, groupId, templateAlias, forceStart))


  def addCourseTemplateToGroup(templateAlias: String, groupId: Long, forceStart: Boolean) =
    for {
      _ <- addCourseToGroupQuery(templateAlias, groupId, forceStart)
      toStartUsersIds <-
        if (forceStart) UserToGroupDao.usersInGroup(groupId)
        else ZIO.succeed(Seq())
      _ <- ZIO.foreach(toStartUsersIds)(userId => CourseOps.startCourseForUser(templateAlias, userId))
    } yield ()

  def removeCourseTemplateFromGroup(templateAlias: String, groupId: Long, forceRemoval: Boolean) =
    for {
      _ <- CourseTemplateForGroupDao.removeCourseFromGroup(templateAlias, groupId)
      toRemoveUserIds <-
        if (forceRemoval) UserToGroupDao.usersInGroup(groupId)
        else ZIO.succeed(Seq())
      _ <- ZIO.foreach(toRemoveUserIds)(userId => CourseOps.removeCourseFromUser(templateAlias, userId))
    } yield ()
}
