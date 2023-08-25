package clientRequests

import viewData.CourseViewData


import CourseData.*
/*for full data*/

object CourseDataJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[CourseDataRequest] = deriveDecoder[CourseDataRequest]
  implicit val reqEnc: Encoder[CourseDataRequest] = deriveEncoder[CourseDataRequest]
  implicit val resDec: Decoder[CourseDataResponse] = deriveDecoder[CourseDataResponse]
  implicit val resEnc: Encoder[CourseDataResponse] = deriveEncoder[CourseDataResponse]

}
import CourseDataJson.*

object CourseData extends Route[CourseDataRequest, CourseDataResponse]{

  override val route: String = "courseData"
}
//REQ
case class CourseDataRequest(token:String, courseId:String) extends WithToken

//RES
sealed trait CourseDataResponse
case class CourseDataSuccess(course:CourseViewData) extends CourseDataResponse

case class CourseNotOwnedByYou() extends CourseDataResponse
case class CourseNotFound() extends CourseDataResponse
case class CourseDataFailure(failure: GenericRequestFailure) extends CourseDataResponse

