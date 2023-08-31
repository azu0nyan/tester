package tester.srv.controller.impl


import zio.*
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.postgres.pgisimplicits.*
import io.github.gaelrenoux.tranzactio.{DbException, doobie}
import doobie.{Connection, Database, TranzactIO, tzio}
import tester.srv.controller.{CoursesService, GroupService, MessageBus}
import tester.srv.controller.impl.CoursesServiceTranzactIO
import tester.srv.dao.CourseTemplateForGroupDao.CourseTemplateForGroup
import tester.srv.dao.{CourseTemplateForGroupDao, UserToGroupDao}
import tester.srv.dao.UserToGroupDao.UserToGroup
import tester.srv.dao.UserToGroupDao.UserToGroup


case class GroupServiceTranzactIO(
                                 bus: MessageBus,
                                 coursesService: CoursesService[TranzactIO]
                                 ) extends GroupService[TranzactIO] {


  def addUserToGroup(userId: Int, groupId: Int): TranzactIO[Boolean] =
    for {
      res <- addUserToGroupQuery(userId, groupId)
      courses <- CourseTemplateForGroupDao.forcedCourses(groupId)
      _ <- ZIO.foreach(courses)(course => coursesService.startCourseForUser(course.templateAlias, userId))
    } yield res

  private def addUserToGroupQuery(userId: Int, groupId: Int) =
    UserToGroupDao.insert(UserToGroup(0, userId, groupId, java.time.Clock.systemUTC().instant(), None))


  def removeUserFromGroup(userId: Int, groupId: Int): TranzactIO[Boolean] = ???

  private def removeUserFromGroupQuery(userId: Int, groupId: Int) =
    UserToGroupDao.updateWhere(fr"leavedat = ${java.time.Clock.systemUTC().instant()} ",
      fr"userId = $userId AND groupId = $groupId")


  def addCourseToGroupQuery(templateAlias: String, groupId: Int, forceStart: Boolean): TranzactIO[Boolean] =
    CourseTemplateForGroupDao.insert(CourseTemplateForGroup(0, groupId, templateAlias, forceStart))


  def addCourseTemplateToGroup(templateAlias: String, groupId: Int, forceStart: Boolean): TranzactIO[Boolean] =
    for {
      res <- addCourseToGroupQuery(templateAlias, groupId, forceStart)
      toStartUsersIds <-
        if (forceStart) UserToGroupDao.usersInGroup(groupId)
        else ZIO.succeed(Seq())
      _ <- ZIO.foreach(toStartUsersIds)(userId => coursesService.startCourseForUser(templateAlias, userId))
    } yield res

  def removeCourseTemplateFromGroup(templateAlias: String, groupId: Int, forceRemoval: Boolean): TranzactIO[Boolean] =
    for {
      res <- CourseTemplateForGroupDao.removeCourseFromGroup(templateAlias, groupId)
      toRemoveUserIds <-
        if (forceRemoval) UserToGroupDao.usersInGroup(groupId)
        else ZIO.succeed(Seq())
      _ <- ZIO.foreach(toRemoveUserIds)(userId => coursesService.removeCourseFromUser(templateAlias, userId))
    } yield res
}

object GroupServiceTranzactIO{
  def live: URIO[MessageBus & CoursesService[TranzactIO], GroupServiceTranzactIO] =
    for {
      bus <- ZIO.service[MessageBus]
      ver <- ZIO.service[CoursesService[TranzactIO]]
    } yield GroupServiceTranzactIO(bus, ver)


  def layer: URLayer[MessageBus & CoursesService[TranzactIO], GroupServiceTranzactIO] =
    ZLayer.fromZIO(live)
}
