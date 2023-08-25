package clientRequests.admin

import clientRequests.{GenericRequestFailure, Route, WithToken}
import viewData.AdminCourseViewData
import AdminCourseInfo.*


object AdminCourseInfoJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[AdminCourseInfoRequest] = deriveDecoder[AdminCourseInfoRequest]
  implicit val reqEnc: Encoder[AdminCourseInfoRequest] = deriveEncoder[AdminCourseInfoRequest]
  implicit val resDec: Decoder[AdminCourseInfoResponse] = deriveDecoder[AdminCourseInfoResponse]
  implicit val resEnc: Encoder[AdminCourseInfoResponse] = deriveEncoder[AdminCourseInfoResponse]

}

import AdminCourseInfoJson.* 

object AdminCourseInfo extends Route[AdminCourseInfoRequest, AdminCourseInfoResponse] {
  override val route: String = "requestAdminCourseInfo"


}

//REQ
case class AdminCourseInfoRequest(token:String, alias:String) extends WithToken

//RES
sealed trait AdminCourseInfoResponse
case class AdminCourseInfoSuccess(courseInfo:AdminCourseViewData) extends AdminCourseInfoResponse

sealed trait AdminCourseInfoFailure extends AdminCourseInfoResponse
case class UnknownAdminCourseInfoFailure() extends AdminCourseInfoFailure

