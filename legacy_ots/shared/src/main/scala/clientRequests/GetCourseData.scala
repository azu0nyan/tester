package clientRequests

import io.circe.generic.auto._
import viewData.CourseViewData

object GetCourseData extends Route[CourseDataRequest, GetCourseDataResponse] {
  override val route: String = "courseData"
}
//REQ
case class CourseDataRequest(token:String, courseId:String)

//RES
sealed trait GetCourseDataResponse
case class GetCourseDataSuccess(course:CourseViewData) extends GetCourseDataResponse

case class GetCourseNotOwnedByYou() extends GetCourseDataResponse
case class GetCourseNotFound() extends GetCourseDataResponse
case class GetCourseDataFailure(failure: GenericRequestFailure) extends GetCourseDataResponse

