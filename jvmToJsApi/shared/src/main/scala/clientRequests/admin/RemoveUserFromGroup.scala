package clientRequests.admin

import clientRequests.{GenericRequestFailure, Route, WithToken}
import RemoveUserFromGroup.*


object RemoveUserFromGroupJson {

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[RemoveUserFromGroupRequest] = deriveDecoder[RemoveUserFromGroupRequest]
  implicit val reqEnc: Encoder[RemoveUserFromGroupRequest] = deriveEncoder[RemoveUserFromGroupRequest]
  implicit val resDec: Decoder[RemoveUserFromGroupResponse] = deriveDecoder[RemoveUserFromGroupResponse]
  implicit val resEnc: Encoder[RemoveUserFromGroupResponse] = deriveEncoder[RemoveUserFromGroupResponse]

}

import RemoveUserFromGroupJson.*

object RemoveUserFromGroup extends Route[RemoveUserFromGroupRequest, RemoveUserFromGroupResponse] {
  override val route: String = "adminRemoveUserToGroup"
}

//REQ
case class RemoveUserFromGroupRequest(token: String, userId: String, groupId: String, forceCourseDelete: Boolean) extends WithToken

//RES
sealed trait RemoveUserFromGroupResponse
case class RemoveUserFromGroupSuccess() extends RemoveUserFromGroupResponse
case class RemoveUserFromGroupFailure() extends RemoveUserFromGroupResponse
