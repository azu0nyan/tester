package clientRequests.admin

import clientRequests.{GenericRequestFailure, Route, WithToken}
import viewData.AdminCourseViewData
import AdminCourseInfo.*


object AdminCourseInfoJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[CourseInfoRequest] = deriveDecoder[CourseInfoRequest]
  implicit val reqEnc: Encoder[CourseInfoRequest] = deriveEncoder[CourseInfoRequest]
  implicit val resDec: Decoder[CourseInfoResponse] = deriveDecoder[CourseInfoResponse]
  implicit val resEnc: Encoder[CourseInfoResponse] = deriveEncoder[CourseInfoResponse]

}

import AdminCourseInfoJson.* 

object AdminCourseInfo extends Route[CourseInfoRequest, CourseInfoResponse] {
  override val route: String = "requestAdminCourseInfo"


}

//REQ
case class CourseInfoRequest(token:String, alias:String) extends WithToken

//RES
sealed trait CourseInfoResponse
case class CourseInfoSuccess(courseInfo:AdminCourseViewData) extends CourseInfoResponse

sealed trait CourseInfoFailure extends CourseInfoResponse
case class UnknownCourseInfoFailure() extends CourseInfoFailure

