package tester.srv.controller

import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
import tester.srv.dao.CourseTemplateForGroupDao.CourseTemplateForGroup
import zio.UIO
trait GroupService {
  def initCaches: TranzactIO[Unit]
  
  def newGroup(title: String, description: String): TranzactIO[Int]
  
  def addUserToGroup(userId: Int, groupId: Int): TranzactIO[Boolean]

  def removeUserFromGroup(userId: Int, groupId: Int): TranzactIO[Boolean]

  def addCourseTemplateToGroup(templateAlias: String, groupId: Int, forceStart: Boolean): TranzactIO[Boolean]

  def removeCourseTemplateFromGroup(templateAlias: String, groupId: Int, forceRemoval: Boolean): TranzactIO[Boolean]
  
  def groupDetailedInfo(groupId: Int): TranzactIO[viewData.GroupDetailedInfoViewData]
  
  def groupCourses(groupId: Int, forced: Boolean): TranzactIO[Seq[CourseTemplateForGroup]]

  def groupInfo(groupId: Int): TranzactIO[viewData.GroupInfoViewData]
  
  def groupUsers(groupId: Int): TranzactIO[Seq[viewData.UserViewData]]
  
  def groupList(): TranzactIO[Seq[viewData.GroupDetailedInfoViewData]]

  def groupScores(groupId: Int, courseAliases: Seq[String], userIds: Seq[Int]): TranzactIO[clientRequests.watcher.LightGroupScores.UserScores]  
 
  def groupUserIds(groupId: Int): UIO[Set[Int]]
  
  def userGroups(userId: Int): UIO[Set[Int]]
}
