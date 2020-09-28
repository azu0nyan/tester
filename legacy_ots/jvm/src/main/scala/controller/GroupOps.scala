package controller

import clientRequests.admin.{AddUserToGroupFailure, AddUserToGroupRequest, AddUserToGroupResponse, AddUserToGroupSuccess, GroupInfoRequest, GroupInfoResponse, GroupInfoResponseFailure, GroupInfoResponseSuccess, GroupListRequest, GroupListResponse, GroupListResponseFailure, GroupListResponseSuccess, NewGroupRequest, NewGroupResponse, NewGroupSuccess, RemoveUserFromGroupFailure, RemoveUserFromGroupRequest, RemoveUserFromGroupResponse, RemoveUserFromGroupSuccess, TitleAlreadyClaimed, UnknownNewGroupFailure}
import clientRequests.watcher.{GroupCourseInfo, GroupProblemInfo, GroupScoresRequest, GroupScoresResponse, GroupScoresSuccess}
import controller.db._
import org.mongodb.scala.bson.ObjectId

object GroupOps {
  def newGroup(req: NewGroupRequest): NewGroupResponse =
    try {
      Group.byIdOrTitle(req.title) match {
        case Some(value) => TitleAlreadyClaimed()
        case None =>
          val g = Group(req.title, "")
          groups.insert(g)
          NewGroupSuccess(g._id.toHexString)
      }
    } catch {
      case _: Throwable => UnknownNewGroupFailure()
    }


  def groupList(req: GroupListRequest): GroupListResponse = {
    try {
      GroupListResponseSuccess(groups.all().map(_.toDetailedViewData))
    } catch {
      case _: Throwable => GroupListResponseFailure()
    }
  }

  def group(req: GroupInfoRequest): GroupInfoResponse = {
    try {
      GroupInfoResponseSuccess(groups.byId(new ObjectId(req.groupId)).get.toDetailedViewData)
    } catch {
      case _: Throwable => GroupInfoResponseFailure()
    }
  }

  //Process requests

  def addUserToGroup(req: AddUserToGroupRequest): AddUserToGroupResponse = {
    User.byIdOrLogin(req.userHexIdOrLogin).flatMap { u =>
      Group.byIdOrTitle(req.groupIdOrTitle).map { g =>
        addUserToGroup(u, g)
        AddUserToGroupSuccess()
      }
    }.getOrElse(AddUserToGroupFailure())
  }

  def removeUserFromGroup(req: RemoveUserFromGroupRequest): RemoveUserFromGroupResponse = {
    User.byIdOrLogin(req.userHexIdOrLogin).flatMap { u =>
      Group.byIdOrTitle(req.groupHexIdOrAlias).map { g =>
        removeUserFromGroup(u, g, req.forceCourseDelete)
        RemoveUserFromGroupSuccess()
      }
    }.getOrElse(RemoveUserFromGroupFailure())
  }


  /** Стартуем все неначатые курсы прявязанные к данной группе */
  def ensureGroupCoursesStarted(user: User, group: Group): Unit = {
    val currentCourses = user.courses
    val requiredCourses = group.templatesForGroup
    requiredCourses.filter(_.forceStartForGroupMembers).foreach { rc =>
      if (!currentCourses.exists(_.templateAlias == rc.templateAlias)) {
        CoursesOps.startCourseForUser(user, rc.template)
      }
    }

  }

  def addUserToGroup(u: User, g: Group): Unit = {
    log.info(s"Adding user ${u.idAndLoginStr} to group ${g.toIdTitleStr}")
    UserToGroup.addUserToGroup(u, g)
    ensureGroupCoursesStarted(u, g)
    GradeOps.addGroupGradesForUser(u, g)
  }


  def removeUserFromGroup(u: User, g: Group, forceCourseDeletion: Boolean): Unit = {
    log.info(s"Removing user ${u.idAndLoginStr} from  group ${g.toIdTitleStr} ${if (forceCourseDeletion) "and forcing dependent courses deletion" else ""} ")
    UserToGroup.removeUserFromGroup(u, g)
    if (forceCourseDeletion) {
      ensureGroupCoursesDeleted(u, g)
    }
    GradeOps.removeAllUserGroupGrades(u, g)
  }


  def ensureGroupCoursesDeleted(user: User, group: Group): Unit = {
    log.info(s"Checking if new courses start needed for ${user.idAndLoginStr} group ${group.toIdTitleStr}")
    val currentCourses = user.courses.map(_.templateAlias).toSet
    val groupCourses = group.templatesForGroup.map(_.templateAlias).toSet
    val otherGroupsCourses = user.groups.filter(_ != group).flatMap(_.templatesForGroup.map(_.templateAlias)).toSet
    val toRemove = (currentCourses & groupCourses) &~ otherGroupsCourses
    toRemove.foreach(CoursesOps.removeCourseFromUserByAlias(user, _))


  }


  def requestGroupScores(req: GroupScoresRequest): GroupScoresResponse = req match {
    case GroupScoresRequest(token, groupId, courseAliases) =>
      val g = db.groups.byId(new ObjectId(groupId)).get
      val coursesTemplates =
        (if (courseAliases.isEmpty) CourseTemplateForGroup.byGroup(g).map(_.templateAlias) else courseAliases).flatMap(TemplatesRegistry.getCourseTemplate)

      val users = g.users
      log.info(s"Getting scores for ${coursesTemplates.size} course and ${users.size} user, from group ${g.title}")
      val usersAndProblems = for (
        u <- users
      ) yield (u.toViewData, {
        for (
          c <- u.courses if coursesTemplates.exists(_.uniqueAlias == c.templateAlias);
          p <- c.ownProblems
        ) yield p.toViewData
      })

      GroupScoresSuccess(coursesTemplates.map(ct =>
        GroupCourseInfo(ct.uniqueAlias, ct.courseTitle,
          ct.problemAliasesToGenerate.map(pa => GroupProblemInfo(pa, TemplatesRegistry.getProblemTemplate(pa).map(_.title(0)).getOrElse("CANT FIND TEMPLATE"))))),
        usersAndProblems)
  }


}
