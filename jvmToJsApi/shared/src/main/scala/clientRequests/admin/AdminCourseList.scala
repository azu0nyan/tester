package clientRequests.admin

import clientRequests.{GenericRequestFailure, Route, WithToken}
import viewData.{CourseViewData, AdminCourseViewData}
import AdminCourseList.*

object AdminCourseListJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[AdminCourseListRequest] = deriveDecoder[AdminCourseListRequest]
  implicit val reqEnc: Encoder[AdminCourseListRequest] = deriveEncoder[AdminCourseListRequest]
  implicit val resDec: Decoder[AdminCourseListResponse] = deriveDecoder[AdminCourseListResponse]
  implicit val resEnc: Encoder[AdminCourseListResponse] = deriveEncoder[AdminCourseListResponse]

}
import AdminCourseListJson.* 

object AdminCourseList extends Route[AdminCourseListRequest, AdminCourseListResponse] {
  override val route: String = "requestCourseList"
  
}

//REQ
case class AdminCourseListRequest(token:String) extends WithToken

//RES
sealed trait AdminCourseListResponse
case class AdminCourseListSuccess(customCourses:Seq[AdminCourseViewData]) extends AdminCourseListResponse

sealed trait AdminCourseListFailure extends AdminCourseListResponse
case class UnknownAdminCourseListFailure() extends AdminCourseListFailure

