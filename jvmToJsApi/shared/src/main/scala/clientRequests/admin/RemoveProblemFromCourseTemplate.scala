package clientRequests.admin

import clientRequests.{GenericRequestFailure, Route, WithToken}
import RemoveProblemFromCourseTemplate.*

object RemoveProblemFromCourseTemplateJson {

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[RemoveProblemFromCourseTemplateRequest] = deriveDecoder[RemoveProblemFromCourseTemplateRequest]
  implicit val reqEnc: Encoder[RemoveProblemFromCourseTemplateRequest] = deriveEncoder[RemoveProblemFromCourseTemplateRequest]
  implicit val resDec: Decoder[RemoveProblemFromCourseTemplateResponse] = deriveDecoder[RemoveProblemFromCourseTemplateResponse]
  implicit val resEnc: Encoder[RemoveProblemFromCourseTemplateResponse] = deriveEncoder[RemoveProblemFromCourseTemplateResponse]

}

import RemoveProblemFromCourseTemplateJson.*

object RemoveProblemFromCourseTemplate extends Route[RemoveProblemFromCourseTemplateRequest, RemoveProblemFromCourseTemplateResponse] {
  override val route: String = "requestRemoveProblemFromCourseTemplate"


}

//REQ
case class RemoveProblemFromCourseTemplateRequest(token: String, courseAlias: String, problemAlias: String) extends WithToken

//RES
sealed trait RemoveProblemFromCourseTemplateResponse
case class RemoveProblemFromCourseTemplateSuccess() extends RemoveProblemFromCourseTemplateResponse

sealed trait RemoveProblemFromCourseTemplateFailure extends RemoveProblemFromCourseTemplateResponse
case class CourseTemplateDoesNotContainsProblemTemplate() extends RemoveProblemFromCourseTemplateFailure
case class UnknownTemplateCourseToRemoveFrom() extends RemoveProblemFromCourseTemplateFailure
case class RemoveProblemFromCourseTemplateUnknownFailure(failure: GenericRequestFailure) extends RemoveProblemFromCourseTemplateFailure

