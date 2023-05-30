package dbMigration

import DbViewsShared.CourseShared.AnswerStatus
import otsbridge.CoursePiece.CourseRoot
import controller.UserRole
import io.getquill.JsonbValue
import otsbridge.AnswerField.AnswerField
import otsbridge.ProblemScore.ProblemScore

object DbModel {
  case class RegisteredUser(id: Int, login: String, passwordHash: String, passwordSalt: String,
                            firstName: Option[String], lastName: Option[String], email: Option[String],
                            registeredAt: java.time.LocalDateTime, lastLogin: Option[java.time.LocalDateTime],
                            role: JsonbValue[UserRole])

  case class UserGroup(id: Int, title: String, description: String)

  case class UserToGroup(id: Int, userId: Int, groupId: Int, enteredAt: java.time.LocalDateTime, leavedAt: Option[java.time.LocalDateTime])

  case class Course(id: Int, userId: Int, templateAlias: String, seed: Int)

  case class CourseTemplateForGroup(id: Int, groupId: Int, templateAlias: String, forceStartForGroupMembers: Boolean)

  case class Problem(id: Int, courseId: Int, templateAlias: String, seed: Int, score: JsonbValue[ProblemScore])

  case class Answer(id: Int, problemId: Int, answer: String, status: JsonbValue[AnswerStatus], answeredAt: java.time.LocalDateTime)

  case class CustomProblemTemplate(alias: String, title: String, html: String,
                                   answerField: JsonbValue[AnswerField], initialScore: JsonbValue[ProblemScore])

  case class CustomCourseTemplate(id: Int, templateAlias: String, description: String, courseData: JsonbValue[CourseRoot])

  case class CustomCourseTemplateProblemAlias(courseId: Int, problemAlias: String)
}
