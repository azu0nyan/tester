package clientRequests

import viewData.{CourseViewData, PartialCourseViewData}
import PartialCourseData.*

object PartialCourseDataJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[PartialCourseDataRequest] = deriveDecoder[PartialCourseDataRequest]
  implicit val reqEnc: Encoder[PartialCourseDataRequest] = deriveEncoder[PartialCourseDataRequest]
  implicit val resDec: Decoder[PartialCourseDataResponse] = deriveDecoder[PartialCourseDataResponse]
  implicit val resEnc: Encoder[PartialCourseDataResponse] = deriveEncoder[PartialCourseDataResponse]

}

import PartialCourseDataJson.* 

/*for full data*/
object PartialCourseData extends Route[PartialCourseDataRequest, PartialCourseDataResponse] {
  override val route: String = "getPartialCourseData"
}
//REQ
case class PartialCourseDataRequest(token:String, courseId:String) extends WithToken

//RES
sealed trait PartialCourseDataResponse
case class PartialCourseDataSuccess(course:PartialCourseViewData) extends PartialCourseDataResponse

case class PartialCourseNotOwnedByYou() extends PartialCourseDataResponse
case class PartialCourseNotFound() extends PartialCourseDataResponse
case class PartialCourseDataFailure(failure: GenericRequestFailure) extends PartialCourseDataResponse


