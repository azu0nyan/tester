package clientRequests.admin

import clientRequests.{GenericRequestFailure, Route, WithToken}
import AddCourseToGroup.*

object AddCourseToGroupJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[AddCourseToGroupRequest] = deriveDecoder[AddCourseToGroupRequest]
  implicit val reqEnc: Encoder[AddCourseToGroupRequest] = deriveEncoder[AddCourseToGroupRequest]
  implicit val resDec: Decoder[AddCourseToGroupResponse] = deriveDecoder[AddCourseToGroupResponse]
  implicit val resEnc: Encoder[AddCourseToGroupResponse] = deriveEncoder[AddCourseToGroupResponse]

}
import AddCourseToGroupJson.*


object AddCourseToGroup extends Route[AddCourseToGroupRequest, AddCourseToGroupResponse] {
  override val route: String = "requestAddCourseToGroup"


  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[AddCourseToGroupRequest] = deriveDecoder[AddCourseToGroupRequest]
  implicit val reqEnc: Encoder[AddCourseToGroupRequest] = deriveEncoder[AddCourseToGroupRequest]
  implicit val resDec: Decoder[AddCourseToGroupResponse] = deriveDecoder[AddCourseToGroupResponse]
  implicit val resEnc: Encoder[AddCourseToGroupResponse] = deriveEncoder[AddCourseToGroupResponse]

}

//REQ
case class AddCourseToGroupRequest(token:String, courseAlias: String, groupId: String, forceToGroupMembers:Boolean) extends WithToken

//RES
sealed trait AddCourseToGroupResponse
case class AddCourseToGroupSuccess() extends AddCourseToGroupResponse
sealed trait AddCourseToGroupFailure extends AddCourseToGroupResponse
case class UnknownAddCourseToGroupFailure() extends AddCourseToGroupFailure

