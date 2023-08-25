package clientRequests.admin

import clientRequests.{GenericRequestFailure, Route, WithToken}
import NewCourseTemplate.*


object NewCourseTemplateJson{
  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[NewCourseTemplateRequest] = deriveDecoder[NewCourseTemplateRequest]
  implicit val reqEnc: Encoder[NewCourseTemplateRequest] = deriveEncoder[NewCourseTemplateRequest]
  implicit val resDec: Decoder[NewCourseTemplateResponse] = deriveDecoder[NewCourseTemplateResponse]
  implicit val resEnc: Encoder[NewCourseTemplateResponse] = deriveEncoder[NewCourseTemplateResponse]

}

import NewCourseTemplateJson.* 

object NewCourseTemplate extends Route[NewCourseTemplateRequest, NewCourseTemplateResponse] {
  override val route: String = "adminNewCustomCourse"
}

//REQ
case class NewCourseTemplateRequest(token: String, uniqueAlias: String) extends WithToken

//RES
sealed trait NewCourseTemplateResponse
case class NewCourseTemplateSuccess(hexId: String) extends NewCourseTemplateResponse
sealed trait NewCourseTemplateFailure extends NewCourseTemplateResponse
case class AliasNotUnique() extends NewCourseTemplateFailure
case class UnknownFailure(failure: GenericRequestFailure) extends NewCourseTemplateFailure


