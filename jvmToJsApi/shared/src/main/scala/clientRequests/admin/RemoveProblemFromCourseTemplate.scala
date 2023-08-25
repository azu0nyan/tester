package clientRequests.admin

import clientRequests.{GenericRequestFailure, Route, WithToken}
import RemoveProblemFromCourseTemplate.*

object RemoveProblemFromCourseTemplateJson {

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[RemoveProblemFromCourseRequest] = deriveDecoder[RemoveProblemFromCourseRequest]
  implicit val reqEnc: Encoder[RemoveProblemFromCourseRequest] = deriveEncoder[RemoveProblemFromCourseRequest]
  implicit val resDec: Decoder[RemoveProblemFromCourseResponse] = deriveDecoder[RemoveProblemFromCourseResponse]
  implicit val resEnc: Encoder[RemoveProblemFromCourseResponse] = deriveEncoder[RemoveProblemFromCourseResponse]

}

import RemoveProblemFromCourseTemplateJson.*

object RemoveProblemFromCourseTemplate extends Route[RemoveProblemFromCourseRequest, RemoveProblemFromCourseResponse] {
  override val route: String = "requestRemoveProblemFromCourseTemplate"


}

//REQ
case class RemoveProblemFromCourseRequest(token: String, courseAlias: String, problemAlias: String) extends WithToken

//RES
sealed trait RemoveProblemFromCourseResponse
case class RemoveProblemFromCourseSuccess() extends RemoveProblemFromCourseResponse

sealed trait RemoveProblemFromCourseFailure extends RemoveProblemFromCourseResponse
case class CourseTemplateDoesNotContainsProblem() extends RemoveProblemFromCourseFailure
case class UnknownCourseToRemoveFrom() extends RemoveProblemFromCourseFailure
case class RemoveProblemFromCourseUnknownFailure(failure: GenericRequestFailure) extends RemoveProblemFromCourseFailure

