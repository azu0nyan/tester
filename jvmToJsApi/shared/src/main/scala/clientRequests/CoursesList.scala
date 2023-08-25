package clientRequests

import viewData.UserCoursesInfoViewData

import CoursesList.*

object CourseListJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[RequestCoursesList] = deriveDecoder[RequestCoursesList]
  implicit val resDec: Decoder[CoursesListResponse] = deriveDecoder[CoursesListResponse]
  implicit val reqEnc: Encoder[RequestCoursesList] = deriveEncoder[RequestCoursesList]
  implicit val resEnc: Encoder[CoursesListResponse] = deriveEncoder[CoursesListResponse]

}

import CourseListJson.* 

object CoursesList  extends Route[RequestCoursesList, CoursesListResponse] {
  override val route: String = "requestCoursesList"
}

//REQ
case class RequestCoursesList(token:String) extends WithToken

//RES
sealed trait CoursesListResponse
case class GetCoursesListSuccess(courses: UserCoursesInfoViewData) extends CoursesListResponse
case class GetCoursesListFailure(failure: GenericRequestFailure) extends CoursesListResponse
