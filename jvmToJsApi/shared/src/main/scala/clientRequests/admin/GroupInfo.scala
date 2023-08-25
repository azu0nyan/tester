package clientRequests.admin

import clientRequests.{Route, WithToken}
import viewData.GroupDetailedInfoViewData
import GroupInfo.*

object GroupInfoJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[GroupInfoRequest] = deriveDecoder[GroupInfoRequest]
  implicit val reqEnc: Encoder[GroupInfoRequest] = deriveEncoder[GroupInfoRequest]
  implicit val resDec: Decoder[GroupInfoResponse] = deriveDecoder[GroupInfoResponse]
  implicit val resEnc: Encoder[GroupInfoResponse] = deriveEncoder[GroupInfoResponse]


}
import GroupInfoJson.* 

object GroupInfo extends Route[GroupInfoRequest, GroupInfoResponse] {
  override val route: String = "adminGetGroupInfo"

}

//REQ
case class GroupInfoRequest(token:String, groupId:String, onlyStudents:Boolean) extends WithToken


//RES
sealed trait GroupInfoResponse
case class GroupInfoResponseSuccess(group:GroupDetailedInfoViewData) extends GroupInfoResponse
case class GroupInfoResponseFailure() extends GroupInfoResponse
