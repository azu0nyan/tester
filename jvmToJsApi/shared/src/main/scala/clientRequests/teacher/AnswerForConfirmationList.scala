package clientRequests.teacher

import clientRequests.{Route, WithToken}
import viewData.{AnswerFullViewData, AnswerViewData}
import AnswerForConfirmationList.*


object AnswerForConfirmationListJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*


  implicit val reqDec1: Decoder[clientRequests.teacher.UserConfirmationInfo] = deriveDecoder[clientRequests.teacher.UserConfirmationInfo]
  implicit val reqEnc1: Encoder[clientRequests.teacher.UserConfirmationInfo] = deriveEncoder[clientRequests.teacher.UserConfirmationInfo]

  implicit val reqDec2: Decoder[clientRequests.teacher.CourseAnswersConfirmationInfo] = deriveDecoder[clientRequests.teacher.CourseAnswersConfirmationInfo]
  implicit val reqEnc2: Encoder[clientRequests.teacher.CourseAnswersConfirmationInfo] = deriveEncoder[clientRequests.teacher.CourseAnswersConfirmationInfo]

  implicit val reqDec3: Decoder[clientRequests.teacher.ShortCourseInfo] = deriveDecoder[clientRequests.teacher.ShortCourseInfo]
  implicit val reqEnc3: Encoder[clientRequests.teacher.ShortCourseInfo] = deriveEncoder[clientRequests.teacher.ShortCourseInfo]


  implicit val reqDec: Decoder[AnswerForConfirmationListRequest] = deriveDecoder[AnswerForConfirmationListRequest]
  implicit val reqEnc: Encoder[AnswerForConfirmationListRequest] = deriveEncoder[AnswerForConfirmationListRequest]
  implicit val resDec: Decoder[AnswerForConfirmationListResponse] = deriveDecoder[AnswerForConfirmationListResponse]
  implicit val resEnc: Encoder[AnswerForConfirmationListResponse] = deriveEncoder[AnswerForConfirmationListResponse]

}

import AnswerForConfirmationListJson.* 

object AnswerForConfirmationList extends Route[AnswerForConfirmationListRequest, AnswerForConfirmationListResponse] {
  override val route: String = "requestAnswerForConfirmationList"
  
}

case class ShortCourseInfo(id: String, templateAlias: String, problemIds: Seq[String])
case class CourseAnswersConfirmationInfo(course: ShortCourseInfo, answer: Seq[AnswerViewData])
case class UserConfirmationInfo(userId: String, courses: Seq[CourseAnswersConfirmationInfo])


//REQ
case class AnswerForConfirmationListRequest(token: String) extends WithToken

//RES
sealed trait AnswerForConfirmationListResponse
case class AnswerForConfirmationListSuccess(userDatas: Seq[UserConfirmationInfo]) extends AnswerForConfirmationListResponse
sealed trait AnswerForConfirmationListFailure extends AnswerForConfirmationListResponse
case class UnknownAnswerForConfirmationListListFailure() extends AnswerForConfirmationListFailure


