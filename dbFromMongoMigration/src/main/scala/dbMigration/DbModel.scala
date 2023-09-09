package dbMigration

import DbViewsShared.CourseShared.AnswerStatus
import otsbridge.CoursePiece.CourseRoot
import controller.UserRole
import otsbridge.AnswerField.AnswerField
import otsbridge.ProblemScore.ProblemScore

object DbModel {
  case class RegisteredUser(id: Int, login: String, passwordHash: String, passwordSalt: String,
                            firstName: String, lastName: String, email: String,
                            registeredAt: java.time.LocalDateTime, lastLogin: Option[java.time.LocalDateTime]                          )

  case class UserGroup(id: Int, title: String, description: String)

  case class UserToGroup(id: Int, userId: Int, groupId: Int, enteredAt: java.time.LocalDateTime, leavedAt: Option[java.time.LocalDateTime])

  case class Course(id: Int, userId: Int, templateAlias: String, seed: Int)

  case class CourseTemplateForGroup(id: Int, groupId: Int, templateAlias: String, forceStartForGroupMembers: Boolean)

  case class Problem(id: Int, courseId: Int, templateAlias: String, seed: Int, scoreNormalized: Double, score: String, addedAt: java.time.LocalDateTime)

  case class Answer(id: Int, problemId: Int, answer: String, answeredAt: java.time.LocalDateTime)

  case class CustomProblemTemplate(alias: String, title: String, html: String,
                                   answerField: String, initialScore: String,
                                   requireConfirmation: Boolean = true, maxAttempts: Option[Int] = None, verificatorAlias: Option[String] = None)

  case class CustomCourseTemplate(templateAlias: String, description: String, courseData: String)

  case class CourseTemplateProblem(courseAlias: String, problemAlias: String)
}
