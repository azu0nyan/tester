package clientRequests.admin

import clientRequests.{GenericRequestFailure, Route, WithToken}
import otsbridge.CoursePiece.CourseRoot
import UpdateCustomCourse.*

object UpdateCustomCourseJson {

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[UpdateCustomCourseRequest] = deriveDecoder[UpdateCustomCourseRequest]
  implicit val reqEnc: Encoder[UpdateCustomCourseRequest] = deriveEncoder[UpdateCustomCourseRequest]
  implicit val resDec: Decoder[UpdateCustomCourseResponse] = deriveDecoder[UpdateCustomCourseResponse]
  implicit val resEnc: Encoder[UpdateCustomCourseResponse] = deriveEncoder[UpdateCustomCourseResponse]

}

import UpdateCustomCourseJson.* 


object UpdateCustomCourse extends Route[UpdateCustomCourseRequest, UpdateCustomCourseResponse] {
  override val route: String = "requestUpdateCustomCourse"


  
}

case class CustomCourseUpdateData(
                                    title: Option[String] = None,
                                    description: Option[String]= None,
                                    courseData: Option[CourseRoot]= None
                                  )

//REQ
case class UpdateCustomCourseRequest(token: String, courseAlias: String, updatedData: CustomCourseUpdateData) extends WithToken

//RES
sealed trait UpdateCustomCourseResponse
case class UpdateCustomCourseSuccess() extends UpdateCustomCourseResponse

sealed trait UpdateCustomCourseFailure extends UpdateCustomCourseResponse
case class UnknownUpdateCustomCourseFailure() extends UpdateCustomCourseFailure

