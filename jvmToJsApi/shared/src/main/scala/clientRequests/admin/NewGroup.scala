package clientRequests.admin

import clientRequests.{GenericRequestFailure, Route, WithToken}
import NewGroup.*

object NewGroupJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[NewGroupRequest] = deriveDecoder[NewGroupRequest]
  implicit val reqEnc: Encoder[NewGroupRequest] = deriveEncoder[NewGroupRequest]
  implicit val resDec: Decoder[NewGroupResponse] = deriveDecoder[NewGroupResponse]
  implicit val resEnc: Encoder[NewGroupResponse] = deriveEncoder[NewGroupResponse]

}
import NewGroupJson.*


object NewGroup extends Route[NewGroupRequest, NewGroupResponse] {
  override val route: String = "requestNewGroup"
}

//REQ
case class NewGroupRequest(token: String, title: String) extends WithToken

//RES
sealed trait NewGroupResponse
case class NewGroupSuccess(groupHexId: String) extends NewGroupResponse
sealed trait NewGroupFailure extends NewGroupResponse
case class TitleAlreadyClaimed() extends NewGroupFailure
case class UnknownNewGroupFailure() extends NewGroupFailure

