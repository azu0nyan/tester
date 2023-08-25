package clientRequests.admin

import clientRequests.{Route, WithToken}
import viewData.GroupDetailedInfoViewData
import GroupList.*

object GroupListJson{

  import viewData.*
  import io.circe.syntax.*, io.circe.*, io.circe.generic.semiauto.*

  implicit val reqDec: Decoder[GroupListRequest] = deriveDecoder[GroupListRequest]
  implicit val reqEnc: Encoder[GroupListRequest] = deriveEncoder[GroupListRequest]
  implicit val resDec: Decoder[GroupListResponse] = deriveDecoder[GroupListResponse]
  implicit val resEnc: Encoder[GroupListResponse] = deriveEncoder[GroupListResponse]


}

import GroupListJson.* 

object GroupList extends Route[GroupListRequest, GroupListResponse] {
  override val route: String = "adminGetGroupList"

}

//REQ
case class GroupListRequest(token:String) extends WithToken


//RES
sealed trait GroupListResponse
case class GroupListResponseSuccess(groups: Seq[GroupDetailedInfoViewData]) extends GroupListResponse
case class GroupListResponseFailure() extends GroupListResponse
