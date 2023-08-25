package clientRequests.admin

import clientRequests.{GenericRequestFailure, Route, WithToken}
import AddUserToGroup.*

object AddUserToGroupJson{
  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[AddUserToGroupRequest] = deriveDecoder[AddUserToGroupRequest]
  implicit val reqEnc: Encoder[AddUserToGroupRequest] = deriveEncoder[AddUserToGroupRequest]
  implicit val resDec: Decoder[AddUserToGroupResponse] = deriveDecoder[AddUserToGroupResponse]
  implicit val resEnc: Encoder[AddUserToGroupResponse] = deriveEncoder[AddUserToGroupResponse]

}
import AddUserToGroupJson.*

object AddUserToGroup extends Route[AddUserToGroupRequest, AddUserToGroupResponse] {
  override val route: String = "adminAddUserToGroup"


}

//REQ
case class AddUserToGroupRequest(token:String, UserHexIdOrLogin:String, groupIdOrTitle:String) extends WithToken

//RES
sealed trait AddUserToGroupResponse
case class AddUserToGroupSuccess() extends AddUserToGroupResponse
case class AddUserToGroupFailure() extends AddUserToGroupResponse

