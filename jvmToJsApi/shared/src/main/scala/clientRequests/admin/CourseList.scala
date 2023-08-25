package clientRequests.admin

import clientRequests.{GenericRequestFailure, Route, WithToken}
import viewData.{CourseViewData, AdminCourseViewData}
import CourseList.*

object CourseListJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[CourseListRequest] = deriveDecoder[CourseListRequest]
  implicit val reqEnc: Encoder[CourseListRequest] = deriveEncoder[CourseListRequest]
  implicit val resDec: Decoder[CourseListResponse] = deriveDecoder[CourseListResponse]
  implicit val resEnc: Encoder[CourseListResponse] = deriveEncoder[CourseListResponse]

}
import CourseListJson.* 

object CourseList extends Route[CourseListRequest, CourseListResponse] {
  override val route: String = "requestCourseList"
  
}

//REQ
case class CourseListRequest(token:String) extends WithToken

//RES
sealed trait CourseListResponse
case class CourseListSuccess(customCourses:Seq[AdminCourseViewData]) extends CourseListResponse

sealed trait CourseListFailure extends CourseListResponse
case class UnknownCourseListFailure() extends CourseListFailure

