package clientRequests

import StartCourse.*

object StartCourseJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[StartCourseRequest] = deriveDecoder[StartCourseRequest]
  implicit val reqEnc: Encoder[StartCourseRequest] = deriveEncoder[StartCourseRequest]
  implicit val resDec: Decoder[StartCourseResponse] = deriveDecoder[StartCourseResponse]
  implicit val resEnc: Encoder[StartCourseResponse] = deriveEncoder[StartCourseResponse]

}

import StartCourseJson.*

object StartCourse extends Route[StartCourseRequest, StartCourseResponse] {
  override val route: String = "requestStartCourse"
  
}
//REQ
case class StartCourseRequest(token:String, courseTemplateAlias:String) extends WithToken

//RES
sealed trait StartCourseResponse
case class RequestStartCourseSuccess(courseHexId: String) extends StartCourseResponse

case class MaximumCourseAttemptsLimitExceeded(attempts:Int) extends StartCourseResponse
case class CourseTemplateNotAvailableForYou() extends StartCourseResponse
case class CourseTemplateNotFound() extends StartCourseResponse
case class RequestStartCourseFailure(failure: GenericRequestFailure) extends StartCourseResponse