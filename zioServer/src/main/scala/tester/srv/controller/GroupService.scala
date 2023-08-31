package tester.srv.controller

import io.github.gaelrenoux.tranzactio.doobie.TranzactIO
trait GroupService {
  def addUserToGroup(userId: Int, groupId: Int): TranzactIO[Boolean]

  def removeUserFromGroup(userId: Int, groupId: Int): TranzactIO[Boolean]

  def addCourseTemplateToGroup(templateAlias: String, groupId: Int, forceStart: Boolean): TranzactIO[Boolean]

  def removeCourseTemplateFromGroup(templateAlias: String, groupId: Int, forceRemoval: Boolean): TranzactIO[Boolean]

}
