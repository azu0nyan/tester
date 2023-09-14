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
import otsbridge.ProblemScore
import tester.srv.controller.{CourseTemplateRegistry, CoursesService, GroupService, MessageBus, UserService}
import tester.srv.controller.impl.CoursesServiceImpl
import tester.srv.dao.CourseTemplateForGroupDao.CourseTemplateForGroup
import tester.srv.dao.{CourseTemplateForGroupDao, GroupDao, UserToGroupDao}
import tester.srv.dao.ProblemDao
import tester.srv.dao.UserToGroupDao.{UserToGroup, usersInGroup}
import zio.concurrent.ConcurrentMap

case class GroupServiceImpl(
                             bus: MessageBus,
                             coursesService: CoursesService,
                             userService: UserService,
                             templateRegistry: CourseTemplateRegistry,
                             userToGroups: ConcurrentMap[Int, Set[Int]],
                             groupToUser: ConcurrentMap[Int, Set[Int]],                             
                           ) extends GroupService {

  def initCaches: TranzactIO[Unit] =
    for {
      g <- GroupDao.all
      _ <- ZIO.logInfo(s"Caching ${g.size} groups.")
      _ <- ZIO.foreach(g) { g =>
        for {
          users <- usersInGroup(g.id)
          _ <- ZIO.logInfo(s"Caching group ${g.title} with ${users.size} users.")
          _ <- groupToUser.put(g.id, users.toSet)
          _ <- ZIO.foreach(users)(uid =>
            userToGroups.compute(uid, {
              case (_, Some(groups)) => Some(groups + g.id)
              case (_, None) => Some(Set(g.id))
            }))
        } yield ()
      }
    } yield ()

  def newGroup(title: String, description: String): TranzactIO[Int] =
    GroupDao.insertReturnId(GroupDao.Group(0, title, description))

  def addUserToGroup(userId: Int, groupId: Int): TranzactIO[Boolean] =
    for {
      res <- addUserToGroupQuery(userId, groupId)
      _ <- userToGroups.compute(userId, {
        case (_, Some(groups)) => Some(groups + groupId)
        case (_, None) => Some(Set(groupId))
      })
      _ <- groupToUser.compute(groupId, {
        case (_, Some(users)) => Some(users + userId)
        case (_, None) => Some(Set(userId))
      })
      courses <- CourseTemplateForGroupDao.groupCourses(groupId, true)
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
      users <- ZIO.foreach(ids)(id => userService.byId(id))
    } yield users

  def groupCourses(groupId: Int, forced: Boolean): TranzactIO[Seq[CourseTemplateForGroup]] =
    CourseTemplateForGroupDao.groupCourses(groupId, forced)

  def groupDetailedInfo(groupId: Int): TranzactIO[viewData.GroupDetailedInfoViewData] =
    for {
      group <- GroupDao.byId(groupId)
      courses <- groupCourses(groupId, false)
      courseTemplates <- ZIO.foreach(courses)(c => templateRegistry.courseTemplate(c.templateAlias))
      coursesViews = courseTemplates.flatten.map(ct => viewData.CourseTemplateViewData(ct.uniqueAlias, ct.courseTitle, ct.description, ct.problemAliasesToGenerate))
      users <- groupUsers(groupId)
    } yield viewData.GroupDetailedInfoViewData(groupId.toString, group.title, group.description, coursesViews, users)

  def groupList(): TranzactIO[Seq[viewData.GroupDetailedInfoViewData]] =
    for {
      groups <- GroupDao.all
      res <- ZIO.foreach(groups)(g => groupDetailedInfo(g.id)) //todo do in one request
    } yield res

  def groupScores(groupId: Int, courseAliases: Seq[String], userIds: Seq[Int]): TranzactIO[clientRequests.watcher.LightGroupScores.UserScores] =
    ProblemDao.queryProblems(
        ProblemDao.ProblemFilter.FromGroupCourses(groupId),
        ProblemDao.ProblemFilter.ByCourseAliases(courseAliases: _*),
        ProblemDao.ProblemFilter.ByUsers(userIds: _*))
      .map { s =>
        s.groupBy(_._2.userId).map {
          case (userId, problems) =>
            val groupedByCourse = problems.groupBy(_._2.courseAlias).map {
              case (courseAlias, courseProblems) =>
                (courseAlias, courseProblems.map(p => (p._1.templateAlias, ProblemScore.fromJson(p._1.score))).toMap)
            }
            (userId.toString, groupedByCourse)
        }
      }

  def groupUserIds(groupId: Int): UIO[Set[Int]] = groupToUser.get(groupId).map(_.getOrElse(Set()))

}

object GroupServiceImpl {
  def live: URIO[MessageBus & CoursesService & UserService & CourseTemplateRegistry, GroupService] =
    for {
      bus <- ZIO.service[MessageBus]
      ver <- ZIO.service[CoursesService]
      usr <- ZIO.service[UserService]
      reg <- ZIO.service[CourseTemplateRegistry]
      m1 <- ConcurrentMap.empty[Int, Set[Int]]
      m2 <- ConcurrentMap.empty[Int, Set[Int]]
    } yield GroupServiceImpl(bus, ver, usr, reg, m1, m2)


  def layer: URLayer[MessageBus & CoursesService & UserService & CourseTemplateRegistry, GroupService] =
    ZLayer.fromZIO(live)
}
