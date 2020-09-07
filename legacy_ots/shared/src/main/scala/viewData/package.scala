import java.time.Instant

import DbViewsShared.CourseShared
import DbViewsShared.CourseShared.{AnswerStatus, CourseStatus}
import otsbridge.{AnswerField, ProblemScore}
import io.circe.generic.auto._
import otsbridge.CoursePiece.{CoursePiece, CourseRoot}
import otsbridge.ProblemScore.ProblemScore

/**
  * Данные которые может послать сервер клиенту
  */
package object viewData {
  /**admin*/
  case class ProblemTemplateExampleViewData(title:String, initialScore:ProblemScore, alias:String, allowedAttempts:Option[Int], exampleHtml:String, answerField:AnswerField)

  /**admin*/
  case class AdminCourseViewData(courseAlias: String, courseTitle: String, description: Option[String],
                                 courseData: CourseRoot, problemAliasesToGenerate: Seq[String], editable:Boolean)

  /**teacher*/
  case class AnswerForConfirmationViewData(answerId:String,answer:String, score: ProblemScore, user:UserViewData, problemViewData: ProblemViewData,  review:Option[String])

  /** Информация о группе пользователе для отображения */
  case class GroupInfoViewData(groupId: String, groupTitle: String, description: String)

  /** Информация о группе пользователе для отображения в админке */
  case class GroupDetailedInfoViewData(groupId: String, groupTitle: String, description: String, courses: Seq[CourseTemplateViewData], users: Seq[UserViewData])

  /** Информация о пользователе для отображения */
  case class UserViewData(id:String, login: String, firstName: Option[String], lastName: Option[String], email: Option[String], groups: Seq[GroupInfoViewData], role: String)

  case class AnswerViewData(
                             answerId:String,
                             problemId: String,
                             answerText: String,
                             answeredAt: Instant,
                             status: AnswerStatus
                           ) {
    def score: Option[ProblemScore] = status match {
      case CourseShared.Verified(score, review, systemMessage, verifiedAt, _) => Some(score)
      case _ => None
    }
  }

  /** Информация о проблеме для отображения пользователю */
  case class ProblemViewData(problemId: String,
                             templateAlias: String,
                             title: String,
                             problemHtml: String,
                             answerFieldType: AnswerField,
                             score: ProblemScore,
                             currentAnswerRaw: String,
                             answers: Seq[AnswerViewData]
                            )

  /** Вся информация о курсе, отображаемая во время его выполнения */
  case class CourseViewData(courseId: String, title: String, status: CourseStatus, courseData: CourseRoot, problems: Seq[ProblemViewData], description: Option[String])

  /** Информация видная в списке активных курсов */
  case class CourseInfoViewData(courseId: String, title: String, status: CourseStatus, description: Option[String])

  /** Информация видная в списке курсов которые можно пройти */
  case class CourseTemplateViewData(courseTemplateAlias: String, title: String, description: Option[String])

  /** Информация видная на странице выбора курса */
  case class UserCoursesInfoViewData(templates: Seq[CourseTemplateViewData], existing: Seq[CourseInfoViewData])
}
