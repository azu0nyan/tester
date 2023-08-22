package tester.srv.controller

trait GroupService[F[_]] {
  def addUserToGroup(userId: Int, groupId: Int): F[Boolean]

  def removeUserFromGroup(userId: Int, groupId: Int): F[Boolean]

  def addCourseTemplateToGroup(templateAlias: String, groupId: Int, forceStart: Boolean): F[Boolean]

  def removeCourseTemplateFromGroup(templateAlias: String, groupId: Int, forceRemoval: Boolean): F[Boolean]

}
