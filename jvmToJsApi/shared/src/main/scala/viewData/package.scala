import java.time.Instant
import DbViewsShared.AnswerStatus
import DbViewsShared.AnswerStatus.{ CourseStatus}
import otsbridge.{AnswerField, ProblemScore}
import io.circe.generic.auto._
import otsbridge.CoursePiece.{CoursePiece, CourseRoot}
import otsbridge.ProblemScore.ProblemScore
import otsbridge.AnswerField._

/**
 * Данные которые может послать сервер клиенту
 */
package object viewData {

  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec1: Decoder[ProblemTemplateExampleViewData] = deriveDecoder[ProblemTemplateExampleViewData]
  implicit val resEnc1: Encoder[ProblemTemplateExampleViewData] = deriveEncoder[ProblemTemplateExampleViewData]
  implicit val reqDec2: Decoder[AdminCourseViewData] = deriveDecoder[AdminCourseViewData]
  implicit val resEnc2: Encoder[AdminCourseViewData] = deriveEncoder[AdminCourseViewData]
  implicit val reqDec3: Decoder[AnswerFullViewData] = deriveDecoder[AnswerFullViewData]
  implicit val resEnc3: Encoder[AnswerFullViewData] = deriveEncoder[AnswerFullViewData]
  implicit val reqDec4: Decoder[GroupInfoViewData] = deriveDecoder[GroupInfoViewData]
  implicit val resEnc4: Encoder[GroupInfoViewData] = deriveEncoder[GroupInfoViewData]
  implicit val reqDec5: Decoder[GroupDetailedInfoViewData] = deriveDecoder[GroupDetailedInfoViewData]
  implicit val resEnc5: Encoder[GroupDetailedInfoViewData] = deriveEncoder[GroupDetailedInfoViewData]
  implicit val reqDec6: Decoder[UserViewData] = deriveDecoder[UserViewData]
  implicit val resEnc6: Encoder[UserViewData] = deriveEncoder[UserViewData]
  implicit val reqDec7: Decoder[ProblemViewData] = deriveDecoder[ProblemViewData]
  implicit val resEnc7: Encoder[ProblemViewData] = deriveEncoder[ProblemViewData]
  implicit val reqDec8: Decoder[CourseViewData] = deriveDecoder[CourseViewData]
  implicit val resEnc8: Encoder[CourseViewData] = deriveEncoder[CourseViewData]
  implicit val reqDec9: Decoder[CourseInfoViewData] = deriveDecoder[CourseInfoViewData]
  implicit val resEnc9: Encoder[CourseInfoViewData] = deriveEncoder[CourseInfoViewData]
  implicit val reqDec10: Decoder[ProblemRefViewData] = deriveDecoder[ProblemRefViewData]
  implicit val resEnc10: Encoder[ProblemRefViewData] = deriveEncoder[ProblemRefViewData]
  implicit val reqDec11: Decoder[PartialCourseViewData] = deriveDecoder[PartialCourseViewData]
  implicit val resEnc11: Encoder[PartialCourseViewData] = deriveEncoder[PartialCourseViewData]
  implicit val reqDec12: Decoder[CourseTemplateViewData] = deriveDecoder[CourseTemplateViewData]
  implicit val resEnc12: Encoder[CourseTemplateViewData] = deriveEncoder[CourseTemplateViewData]
  implicit val reqDec13: Decoder[AnswerViewData] = deriveDecoder[AnswerViewData]
  implicit val resEnc13: Encoder[AnswerViewData] = deriveEncoder[AnswerViewData]


  /** admin */
  case class ProblemTemplateExampleViewData(title: String, initialScore: ProblemScore, alias: String,
                                            allowedAttempts: Option[Int], exampleHtml: String, answerField: AnswerField,
                                            editable: Boolean)

  /** admin */
  case class AdminCourseViewData(courseAlias: String, courseTitle: String, description: String,
                                 courseData: CourseRoot, problemAliasesToGenerate: Seq[String], editable: Boolean)

  /** teacher */
  case class AnswerFullViewData(answerId: String, answer: String, answeredAt: Instant,score: ProblemScore, user: UserViewData, problemViewData: ProblemViewData, review: Option[String])


  /** Информация о группе пользователе для отображения */
  case class GroupInfoViewData(groupId: String, groupTitle: String, description: String)

  /** Информация о группе пользователе для отображения в админке */
  case class GroupDetailedInfoViewData(groupId: String, groupTitle: String, description: String, courses: Seq[CourseTemplateViewData], users: Seq[UserViewData])

  /** Информация о пользователе для отображения */
  case class UserViewData(id: String, login: String, firstName: Option[String], lastName: Option[String], email: Option[String], groups: Seq[GroupInfoViewData], role: String, registeredAt: Instant) {
    def loginNameString: String = s"${login} ${firstName.getOrElse("")} ${lastName.getOrElse("")}"

  }

  case class AnswerViewData(
                             answerId: String,
                             problemId: String,
                             answerText: String,
                             answeredAt: Instant,
                             status: AnswerStatus
                           ) {
    def score: Option[ProblemScore] = status match {
      case AnswerStatus.Verified(score, review, systemMessage, verifiedAt, _) => Some(score)
      case AnswerStatus.VerifiedAwaitingConfirmation(score, _, _) => Some(score)
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
  case class CourseViewData(courseId: String, title: String, status: CourseStatus, courseData: CourseRoot, problems: Seq[ProblemViewData], description: String)

  /** Информация видная в списке активных курсов */
  case class CourseInfoViewData(courseId: String, title: String, status: CourseStatus, description: String)

  /** Информация видная в списке курсов которые можно пройти */
  case class CourseTemplateViewData(courseTemplateAlias: String, title: String, description: String, problems: Seq[String])

  /** Информация видная на странице выбора курса */
  case class UserCoursesInfoViewData(templates: Seq[CourseTemplateViewData], existing: Seq[CourseInfoViewData])

  /** Краткая информация о задаче в списках задач */
  case class ProblemRefViewData(problemId: String, templateAlias: String, title: String,  score: ProblemScore)
  /** Вся информация о курсе, отображаемая во время его выполнения */
  case class PartialCourseViewData(courseId: String, title: String, description: String, status: CourseStatus, courseData: CourseRoot, problems: Seq[ProblemRefViewData]){
    def refByAlias(alias: String): Option[ProblemRefViewData] = problems.find(_.templateAlias == alias)
  }


}
