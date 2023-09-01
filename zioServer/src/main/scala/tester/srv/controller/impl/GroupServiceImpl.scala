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
import tester.srv.controller.{CoursesService, GroupService, MessageBus, UserService}
import tester.srv.controller.impl.CoursesServiceImpl
import tester.srv.dao.CourseTemplateForGroupDao.CourseTemplateForGroup
import tester.srv.dao.{CourseTemplateForGroupDao, UserToGroupDao, GroupDao}
import tester.srv.dao.UserToGroupDao.UserToGroup
import tester.srv.dao.UserToGroupDao.UserToGroup


case class GroupServiceImpl(
                             bus: MessageBus,
                             coursesService: CoursesService,
                             userService: UserService
                           ) extends GroupService {

  def newGroup(title: String, description: String): TranzactIO[Int] =
    GroupDao.insertReturnId(GroupDao.Group(0, title, description))

  def addUserToGroup(userId: Int, groupId: Int): TranzactIO[Boolean] =
    for {
      res <- addUserToGroupQuery(userId, groupId)
      courses <- CourseTemplateForGroupDao.groupCourses(groupId)
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

  def groupInfo(groupId: Int): TranzactIO[viewData.GroupInfoViewData] =
    for {
      group <- GroupDao.byId(groupId)
    } yield viewData.GroupInfoViewData(groupId.toString, group.title, group.description)

  def groupUsers(groupId: Int): TranzactIO[Seq[viewData.UserViewData]] =
    for {
      ids <- UserToGroupDao.usersInGroup(groupId)
      users <- ZIO.foreach(ids)(id => UserService.byId(id))
    } yield users

  def groupCourses(groupId: Int, forced: Boolean): TranzactIO[Seq[CourseTemplateForGroup]] =
    CourseTemplateForGroupDao.groupCourses(groupId, forced)

  def groupDetailedInfo(groupId: Int): TranzactIO[viewData.GroupDetailedInfoViewData] =
    for {
      group <- GroupDao.byId(groupId)
      courses <- groupCourses(groupId, false)
      coursesViews <- ZIO.foreach(courses)(c => coursesService.courseViewData(c.templateAlias))
      users <- groupUsers(groupId)
    } yield viewData.GroupDetailedInfoViewData(groupId.toString, group.title, group.description, coursesViews, users)

  def groupList(): TranzactIO[Seq[viewData.GroupInfoViewData]] =
    for {
      groups <- GroupDao.all
      res <- ZIO.foreach(groups)(g => groupInfo(g.id))
    } yield res
}

object GroupServiceImpl {
  def live: URIO[MessageBus & CoursesService, GroupService] =
    for {
      bus <- ZIO.service[MessageBus]
      ver <- ZIO.service[CoursesService]
    } yield GroupServiceImpl(bus, ver)


  def layer: URLayer[MessageBus & CoursesService, GroupService] =
    ZLayer.fromZIO(live)
}
